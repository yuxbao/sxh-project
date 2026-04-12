package com.yizhaoqi.sxh.rag.service;

import com.yizhaoqi.sxh.rag.client.DeepSeekClient;
import com.yizhaoqi.sxh.rag.entity.SearchResult;
import com.yizhaoqi.sxh.rag.model.ArticleKnowledge;
import com.yizhaoqi.sxh.rag.model.FileUpload;
import com.yizhaoqi.sxh.rag.repository.ArticleKnowledgeRepository;
import com.yizhaoqi.sxh.rag.repository.DocumentVectorRepository;
import com.yizhaoqi.sxh.rag.repository.FileUploadRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleKnowledgeService {

    private static final String ARTICLE_ORG_TAG = "SXH_ARTICLE";

    private final ArticleKnowledgeRepository articleKnowledgeRepository;
    private final FileUploadRepository fileUploadRepository;
    private final DocumentVectorRepository documentVectorRepository;
    private final ElasticsearchService elasticsearchService;
    private final ParseService parseService;
    private final VectorizationService vectorizationService;
    private final HybridSearchService hybridSearchService;
    private final DeepSeekClient deepSeekClient;
    private final MinioClient minioClient;

    @Value("${minio.bucketName:uploads}")
    private String bucketName;

    @Transactional
    public void upsertArticle(ArticleSyncPayload payload) {
        String fileMd5 = buildArticleFileMd5(payload.articleId());
        ArticleKnowledge existingKnowledge = articleKnowledgeRepository.findByArticleId(payload.articleId()).orElse(null);
        String previousFileName = existingKnowledge != null ? existingKnowledge.getFileName() : null;
        String fileName = buildArticleFileName(payload.articleId(), payload.title());
        byte[] contentBytes = buildMarkdown(payload).getBytes(StandardCharsets.UTF_8);

        uploadMergedObject(fileName, contentBytes);
        clearIndexedData(fileMd5);
        upsertFileUpload(fileMd5, fileName, payload, contentBytes.length);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes)) {
            parseService.parseAndSave(fileMd5, inputStream, String.valueOf(payload.authorId()), ARTICLE_ORG_TAG, true);
        } catch (Exception e) {
            throw new RuntimeException("解析文章语料失败", e);
        }

        vectorizationService.vectorize(fileMd5, String.valueOf(payload.authorId()), ARTICLE_ORG_TAG, true);
        upsertArticleKnowledge(fileMd5, fileName, payload);
        removeLegacyMergedObject(previousFileName, fileName);

        log.info("文章语料同步成功, articleId={}, fileMd5={}", payload.articleId(), fileMd5);
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        ArticleKnowledge knowledge = articleKnowledgeRepository.findByArticleId(articleId).orElse(null);
        String fileMd5 = knowledge != null ? knowledge.getFileMd5() : buildArticleFileMd5(articleId);
        String fileName = knowledge != null ? knowledge.getFileName() : buildArticleFileName(articleId);

        clearIndexedData(fileMd5);
        articleKnowledgeRepository.deleteByArticleId(articleId);
        fileUploadRepository.deleteByFileMd5(fileMd5);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object("merged/" + fileName).build());
        } catch (Exception e) {
            log.warn("删除 MinIO 文章对象失败, articleId={}, fileName={}", articleId, fileName, e);
        }

        log.info("文章语料删除成功, articleId={}, fileMd5={}", articleId, fileMd5);
    }

    public RagChatResponse chat(ArticleChatPayload payload) {
        List<SearchResult> searchResults = hybridSearchService.search(payload.question(), payload.topK());
        String context = buildContext(searchResults);
        String answer = deepSeekClient.generateResponse(payload.question(), context, payload.history());
        return new RagChatResponse(
                answer,
                buildReferences(searchResults),
                buildRecommendations(searchResults)
        );
    }

    public List<ArticleRecommendation> search(String query, int topK) {
        return buildRecommendations(hybridSearchService.search(query, topK));
    }

    private void uploadMergedObject(String fileName, byte[] contentBytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object("merged/" + fileName)
                            .stream(inputStream, contentBytes.length, -1)
                            .contentType("text/markdown; charset=UTF-8")
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传文章语料到 MinIO 失败", e);
        }
    }

    private void upsertFileUpload(String fileMd5, String fileName, ArticleSyncPayload payload, long totalSize) {
        FileUpload fileUpload = fileUploadRepository.findByFileMd5(fileMd5).orElseGet(FileUpload::new);
        fileUpload.setFileMd5(fileMd5);
        fileUpload.setFileName(fileName);
        fileUpload.setTotalSize(totalSize);
        fileUpload.setStatus(1);
        fileUpload.setUserId(String.valueOf(payload.authorId()));
        fileUpload.setOrgTag(ARTICLE_ORG_TAG);
        fileUpload.setPublic(true);
        fileUploadRepository.save(fileUpload);
    }

    private void upsertArticleKnowledge(String fileMd5, String fileName, ArticleSyncPayload payload) {
        ArticleKnowledge knowledge = articleKnowledgeRepository.findByArticleId(payload.articleId())
                .orElseGet(ArticleKnowledge::new);
        knowledge.setArticleId(payload.articleId());
        knowledge.setFileMd5(fileMd5);
        knowledge.setAuthorId(payload.authorId());
        knowledge.setAuthorName(payload.authorName());
        knowledge.setTitle(payload.title());
        knowledge.setSummary(payload.summary());
        knowledge.setCategoryName(payload.categoryName());
        knowledge.setTagsText(String.join(",", payload.tags()));
        knowledge.setArticleUrl(payload.articleUrl());
        knowledge.setFileName(fileName);
        articleKnowledgeRepository.save(knowledge);
    }

    private void clearIndexedData(String fileMd5) {
        try {
            elasticsearchService.deleteByFileMd5(fileMd5);
        } catch (Exception e) {
            log.warn("删除 ES 旧索引失败, fileMd5={}", fileMd5, e);
        }
        documentVectorRepository.deleteByFileMd5(fileMd5);
    }

    private String buildContext(List<SearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "";
        }

        Map<String, ArticleKnowledge> knowledgeMap = articleKnowledgeRepository.findByFileMd5In(
                        searchResults.stream().map(SearchResult::getFileMd5).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(ArticleKnowledge::getFileMd5, item -> item));

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < searchResults.size(); i++) {
            SearchResult result = searchResults.get(i);
            ArticleKnowledge knowledge = knowledgeMap.get(result.getFileMd5());
            String label = knowledge != null ? knowledge.getTitle() : Optional.ofNullable(result.getFileName()).orElse("unknown");
            String snippet = result.getTextContent();
            if (snippet.length() > 320) {
                snippet = snippet.substring(0, 320) + "…";
            }
            context.append("[").append(i + 1).append("] ")
                    .append("(").append(label).append(") ")
                    .append(snippet)
                    .append("\n");
        }
        return context.toString();
    }

    private List<ArticleReference> buildReferences(List<SearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, ArticleKnowledge> knowledgeMap = articleKnowledgeRepository.findByFileMd5In(
                        searchResults.stream().map(SearchResult::getFileMd5).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(ArticleKnowledge::getFileMd5, item -> item));

        return searchResults.stream().map(result -> {
            ArticleKnowledge knowledge = knowledgeMap.get(result.getFileMd5());
            return new ArticleReference(
                    knowledge != null ? knowledge.getArticleId() : null,
                    knowledge != null ? knowledge.getTitle() : result.getFileName(),
                    knowledge != null ? knowledge.getArticleUrl() : null,
                    result.getScore(),
                    result.getTextContent()
            );
        }).toList();
    }

    private List<ArticleRecommendation> buildRecommendations(List<SearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> scoreMap = new LinkedHashMap<>();
        for (SearchResult searchResult : searchResults) {
            scoreMap.putIfAbsent(searchResult.getFileMd5(), searchResult.getScore());
        }

        Map<String, ArticleKnowledge> knowledgeMap = articleKnowledgeRepository.findByFileMd5In(scoreMap.keySet())
                .stream()
                .collect(Collectors.toMap(ArticleKnowledge::getFileMd5, item -> item));

        List<ArticleRecommendation> recommendations = new ArrayList<>();
        for (Map.Entry<String, Double> entry : scoreMap.entrySet()) {
            ArticleKnowledge knowledge = knowledgeMap.get(entry.getKey());
            if (knowledge == null) {
                continue;
            }
            recommendations.add(new ArticleRecommendation(
                    knowledge.getArticleId(),
                    knowledge.getTitle(),
                    knowledge.getSummary(),
                    knowledge.getArticleUrl(),
                    entry.getValue()
            ));
        }
        return recommendations;
    }

    private String buildMarkdown(ArticleSyncPayload payload) {
        String tags = payload.tags().isEmpty() ? "" : String.join(", ", payload.tags());
        return """
                # %s

                - articleId: %d
                - authorId: %d
                - authorName: %s
                - category: %s
                - tags: %s
                - url: %s
                - summary: %s

                ## 正文

                %s
                """.formatted(
                payload.title(),
                payload.articleId(),
                payload.authorId(),
                safe(payload.authorName()),
                safe(payload.categoryName()),
                safe(tags),
                safe(payload.articleUrl()),
                safe(payload.summary()),
                safe(payload.content())
        );
    }

    private String buildArticleFileMd5(Long articleId) {
        return DigestUtils.md5DigestAsHex(("sxh-article-" + articleId).getBytes(StandardCharsets.UTF_8));
    }

    private String buildArticleFileName(Long articleId) {
        return "sxh-article-" + articleId + ".md";
    }

    private String buildArticleFileName(Long articleId, String title) {
        String normalizedTitle = Optional.ofNullable(title)
                .map(String::trim)
                .map(value -> value.replaceAll("[\\\\/:*?\"<>|]", "-"))
                .map(value -> value.replaceAll("\\s+", " "))
                .orElse("");
        if (normalizedTitle.isBlank()) {
            return buildArticleFileName(articleId);
        }
        String shortenedTitle = normalizedTitle.length() > 80 ? normalizedTitle.substring(0, 80).trim() : normalizedTitle;
        return shortenedTitle + "-" + articleId + ".md";
    }

    private void removeLegacyMergedObject(String previousFileName, String currentFileName) {
        if (previousFileName == null || previousFileName.isBlank() || previousFileName.equals(currentFileName)) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object("merged/" + previousFileName).build());
        } catch (Exception e) {
            log.warn("删除旧文章语料对象失败, previousFileName={}", previousFileName, e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record ArticleSyncPayload(
            Long articleId,
            Long authorId,
            String authorName,
            String title,
            String summary,
            String content,
            String categoryName,
            List<String> tags,
            String articleUrl
    ) {
        public ArticleSyncPayload {
            tags = tags == null ? Collections.emptyList() : tags;
        }
    }

    public record ArticleChatPayload(
            Long userId,
            String userName,
            String conversationId,
            String question,
            List<Map<String, String>> history,
            Integer topK
    ) {
        public ArticleChatPayload {
            history = history == null ? Collections.emptyList() : history;
            topK = topK == null || topK <= 0 ? 5 : topK;
        }
    }

    public record RagChatResponse(
            String answer,
            List<ArticleReference> references,
            List<ArticleRecommendation> recommendations
    ) {
    }

    public record ArticleReference(
            Long articleId,
            String title,
            String url,
            Double score,
            String snippet
    ) {
    }

    public record ArticleRecommendation(
            Long articleId,
            String title,
            String summary,
            String url,
            Double score
    ) {
    }
}

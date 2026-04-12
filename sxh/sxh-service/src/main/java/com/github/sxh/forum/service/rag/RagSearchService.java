package com.github.sxh.forum.service.rag;

import com.github.sxh.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.sxh.forum.service.article.service.ArticleReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagSearchService {

    private static final int MAX_SUGGESTIONS = 10;

    private final ArticleReadService articleReadService;
    private final RagClient ragClient;

    public List<SimpleArticleDTO> querySuggestArticles(String key) {
        if (!StringUtils.hasText(key)) {
            return List.of();
        }

        String query = key.trim();
        LinkedHashMap<Long, SimpleArticleDTO> merged = new LinkedHashMap<>();
        List<SimpleArticleDTO> keywordMatches = articleReadService.querySimpleArticleBySearchKey(query);
        if (keywordMatches == null) {
            keywordMatches = Collections.emptyList();
        }
        keywordMatches.forEach(item -> merge(merged, item));

        if (query.length() < 2) {
            return limit(new ArrayList<>(merged.values()));
        }

        try {
            ragClient.search(new RagSearchReq(query, 5)).forEach(item -> {
                if (item.articleId() == null) {
                    return;
                }
                SimpleArticleDTO article = new SimpleArticleDTO()
                        .setId(item.articleId())
                        .setTitle(item.title())
                        .setColumn("AI语义推荐");
                merge(merged, article);
            });
        } catch (Exception e) {
            log.warn("RAG 搜索建议查询失败，降级为站内关键词搜索, key={}", query, e);
        }

        List<SimpleArticleDTO> result = new ArrayList<>(merged.values());
        return limit(result);
    }

    private void merge(LinkedHashMap<Long, SimpleArticleDTO> merged, SimpleArticleDTO article) {
        if (article == null || article.getId() == null) {
            return;
        }
        merged.putIfAbsent(article.getId(), article);
    }

    private List<SimpleArticleDTO> limit(List<SimpleArticleDTO> articles) {
        return new ArrayList<>(articles.subList(0, Math.min(articles.size(), MAX_SUGGESTIONS)));
    }
}

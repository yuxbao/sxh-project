package com.github.sxh.forum.service.rag;

import com.github.sxh.forum.api.model.enums.PushStatusEnum;
import com.github.sxh.forum.api.model.enums.YesOrNoEnum;
import com.github.sxh.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.sxh.forum.api.model.vo.article.dto.TagDTO;
import com.github.sxh.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.sxh.forum.service.article.repository.dao.ArticleDao;
import com.github.sxh.forum.service.article.repository.entity.ArticleDO;
import com.github.sxh.forum.service.article.service.ArticleReadService;
import com.github.sxh.forum.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagSyncService {

    private final RagClient ragClient;
    private final ArticleDao articleDao;
    private final ArticleReadService articleReadService;
    private final UserService userService;

    @Value("${view.site.host:http://127.0.0.1:8082}")
    private String siteHost;

    public void syncArticleByStatusQuietly(Long articleId) {
        try {
            ArticleDO article = articleReadService.queryBasicArticle(articleId);
            if (article == null
                    || article.getDeleted() == YesOrNoEnum.YES.getCode()
                    || article.getStatus() != PushStatusEnum.ONLINE.getCode()) {
                deleteArticleQuietly(articleId);
                return;
            }

            syncArticleQuietly(articleId);
        } catch (Exception e) {
            log.error("按状态同步文章到 RAG 失败, articleId={}", articleId, e);
        }
    }

    public void syncArticleQuietly(Long articleId) {
        try {
            ArticleDTO article = articleReadService.queryDetailArticleInfo(articleId);
            BaseUserInfoDTO author = userService.queryBasicUserInfo(article.getAuthor());
            List<String> tags = article.getTags() == null
                    ? Collections.emptyList()
                    : article.getTags().stream().map(TagDTO::getTag).toList();

            ragClient.upsertArticle(new RagArticleSyncReq(
                    article.getArticleId(),
                    article.getAuthor(),
                    author.getUserName(),
                    article.getTitle(),
                    article.getSummary(),
                    article.getContent(),
                    article.getCategory() != null ? article.getCategory().getCategory() : null,
                    tags,
                    buildArticleUrl(article.getArticleId())
            ));
        } catch (Exception e) {
            log.error("同步文章到 RAG 失败, articleId={}", articleId, e);
        }
    }

    public void deleteArticleQuietly(Long articleId) {
        try {
            ragClient.deleteArticle(articleId);
        } catch (Exception e) {
            log.error("删除 RAG 文章语料失败, articleId={}", articleId, e);
        }
    }

    @Async
    public void reconcileAllArticlesAsync() {
        List<Long> articleIds = articleDao.lambdaQuery()
                .orderByAsc(ArticleDO::getId)
                .list()
                .stream()
                .map(ArticleDO::getId)
                .toList();

        log.info("开始全量对账文章到 RAG，total={}", articleIds.size());
        for (int i = 0; i < articleIds.size(); i++) {
            syncArticleByStatusQuietly(articleIds.get(i));
            if ((i + 1) % 20 == 0 || i + 1 == articleIds.size()) {
                log.info("文章 RAG 对账进度: {}/{}", i + 1, articleIds.size());
            }
        }
        log.info("文章 RAG 对账完成，total={}", articleIds.size());
    }

    private String buildArticleUrl(Long articleId) {
        return siteHost + "/article/detail/" + articleId;
    }
}

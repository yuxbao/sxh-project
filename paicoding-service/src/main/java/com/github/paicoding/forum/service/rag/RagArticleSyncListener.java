package com.github.paicoding.forum.service.rag;

import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import com.github.paicoding.forum.api.model.event.ArticleMsgEvent;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagArticleSyncListener {

    private final RagSyncService ragSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onArticleChanged(ArticleMsgEvent<ArticleDO> event) {
        ArticleDO article = event.getContent();
        if (article == null || article.getId() == null) {
            return;
        }

        ArticleEventEnum type = event.getType();
        if (type == null) {
            return;
        }

        log.info("收到文章事件，准备同步到 RAG: articleId={}, event={}", article.getId(), type.name());
        ragSyncService.syncArticleByStatusQuietly(article.getId());
    }
}

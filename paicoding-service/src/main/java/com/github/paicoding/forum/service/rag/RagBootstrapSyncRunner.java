package com.github.paicoding.forum.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagBootstrapSyncRunner {

    private final RagSyncService ragSyncService;

    @Value("${sxh.rag.sync-on-startup:true}")
    private boolean syncOnStartup;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!syncOnStartup) {
            log.info("已关闭启动时文章回灌到 RAG");
            return;
        }

        log.info("应用启动完成，开始异步对账全部文章到 RAG");
        ragSyncService.reconcileAllArticlesAsync();
    }
}

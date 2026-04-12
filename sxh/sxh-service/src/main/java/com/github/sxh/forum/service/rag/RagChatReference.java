package com.github.sxh.forum.service.rag;

public record RagChatReference(
        Long articleId,
        String title,
        String url,
        Double score,
        String snippet
) {
}

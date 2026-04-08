package com.github.paicoding.forum.service.rag;

public record RagChatReference(
        Long articleId,
        String title,
        String url,
        Double score,
        String snippet
) {
}

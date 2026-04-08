package com.github.paicoding.forum.service.rag;

public record RagChatRecommendation(
        Long articleId,
        String title,
        String summary,
        String url,
        Double score
) {
}

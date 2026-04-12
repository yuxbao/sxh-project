package com.github.sxh.forum.service.rag;

public record RagChatRecommendation(
        Long articleId,
        String title,
        String summary,
        String url,
        Double score
) {
}

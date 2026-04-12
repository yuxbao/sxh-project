package com.github.sxh.forum.service.rag;

import java.util.List;

public record RagArticleSyncReq(
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
}

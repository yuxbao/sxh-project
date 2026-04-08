package com.github.paicoding.forum.service.rag;

import java.util.List;

public record RagChatReq(
        Long userId,
        String userName,
        String conversationId,
        String question,
        List<RagChatHistoryItem> history,
        Integer topK
) {
}

package com.github.paicoding.forum.service.rag;

import java.util.List;

public record RagChatResp(
        String answer,
        List<RagChatReference> references,
        List<RagChatRecommendation> recommendations
) {
}

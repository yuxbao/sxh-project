package com.github.sxh.forum.service.rag;

import java.util.List;

public record RagChatResp(
        String answer,
        List<RagChatReference> references,
        List<RagChatRecommendation> recommendations
) {
}

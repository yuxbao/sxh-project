package com.github.paicoding.forum.service.rag;

public record RagSearchReq(
        String query,
        Integer topK
) {
    public RagSearchReq {
        query = query == null ? "" : query.trim();
        topK = topK == null || topK <= 0 ? 5 : topK;
    }
}

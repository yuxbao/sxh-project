package com.github.paicoding.forum.service.rag;

public record RagSsoLoginReq(
        Long sxhUserId,
        String userName,
        String displayName,
        String avatar,
        String role
) {
}

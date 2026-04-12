package com.yizhaoqi.sxh.rag.controller;

import com.yizhaoqi.sxh.rag.service.ArticleKnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final ArticleKnowledgeService articleKnowledgeService;
    private final com.yizhaoqi.sxh.rag.service.SxhBridgeAuthService sxhBridgeAuthService;

    @Value("${integration.sxh.token:sxh-rag-internal-token}")
    private String internalToken;

    @PostMapping("/articles/upsert")
    public ResponseEntity<?> upsertArticle(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody ArticleUpsertRequest request) {
        if (!authorized(token)) {
            return unauthorized();
        }

        articleKnowledgeService.upsertArticle(new ArticleKnowledgeService.ArticleSyncPayload(
                request.articleId(),
                request.authorId(),
                request.authorName(),
                request.title(),
                request.summary(),
                request.content(),
                request.categoryName(),
                request.tags(),
                request.articleUrl()
        ));

        return ResponseEntity.ok(Map.of("code", 200, "message", "ok"));
    }

    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<?> deleteArticle(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @PathVariable Long articleId) {
        if (!authorized(token)) {
            return unauthorized();
        }

        articleKnowledgeService.deleteArticle(articleId);
        return ResponseEntity.ok(Map.of("code", 200, "message", "ok"));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody AssistantChatRequest request) {
        if (!authorized(token)) {
            return unauthorized();
        }

        ArticleKnowledgeService.RagChatResponse response = articleKnowledgeService.chat(
                new ArticleKnowledgeService.ArticleChatPayload(
                        request.userId(),
                        request.userName(),
                        request.conversationId(),
                        request.question(),
                        request.history(),
                        request.topK()
                )
        );
        return ResponseEntity.ok(Map.of("code", 200, "message", "ok", "data", response));
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody SearchRequest request) {
        if (!authorized(token)) {
            return unauthorized();
        }

        List<ArticleKnowledgeService.ArticleRecommendation> recommendations =
                articleKnowledgeService.search(request.query(), request.topK() == null ? 5 : request.topK());
        return ResponseEntity.ok(Map.of("code", 200, "message", "ok", "data", recommendations));
    }

    @PostMapping("/sso/login")
    public ResponseEntity<?> createSsoLogin(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody SxhSsoLoginRequest request) {
        if (!authorized(token)) {
            return unauthorized();
        }

        String bridgeCode = sxhBridgeAuthService.createBridgeCode(
                new com.yizhaoqi.sxh.rag.service.SxhBridgeAuthService.SxhUserPayload(
                        request.sxhUserId(),
                        request.userName(),
                        request.displayName(),
                        request.avatar(),
                        request.role()
                )
        );
        return ResponseEntity.ok(Map.of("code", 200, "message", "ok", "data", Map.of("bridgeCode", bridgeCode)));
    }

    private boolean authorized(String token) {
        return internalToken.equals(token);
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Internal")
                .body(Map.of("code", 401, "message", "unauthorized"));
    }
}

record ArticleUpsertRequest(
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

record AssistantChatRequest(
        Long userId,
        String userName,
        String conversationId,
        String question,
        List<Map<String, String>> history,
        Integer topK
) {
}

record SearchRequest(
        String query,
        Integer topK
) {
}

record SxhSsoLoginRequest(
        Long sxhUserId,
        String userName,
        String displayName,
        String avatar,
        String role
) {
}

package com.github.paicoding.forum.service.rag;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class RagClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String internalToken;

    public RagClient(@Value("${sxh.rag.base-url:http://localhost:8081}") String baseUrl,
                     @Value("${sxh.rag.token:sxh-rag-internal-token}") String internalToken) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.internalToken = internalToken;
    }

    public void upsertArticle(RagArticleSyncReq request) {
        postWithoutResponse("/api/v1/integration/articles/upsert", request);
    }

    public void deleteArticle(Long articleId) {
        try {
            HttpRequest httpRequest = baseRequest("/api/v1/integration/articles/" + articleId)
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            validateResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("删除 RAG 文章语料失败", e);
        }
    }

    public RagChatResp chat(RagChatReq request) {
        try {
            HttpResponse<String> response = post("/api/v1/integration/chat", request);
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");
            return objectMapper.treeToValue(data, RagChatResp.class);
        } catch (Exception e) {
            throw new RuntimeException("调用 RAG 聊天失败", e);
        }
    }

    public List<RagChatRecommendation> search(RagSearchReq request) {
        try {
            HttpResponse<String> response = post("/api/v1/integration/search", request);
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return List.of();
            }
            JavaType type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, RagChatRecommendation.class);
            return objectMapper.readValue(data.traverse(), type);
        } catch (Exception e) {
            throw new RuntimeException("调用 RAG 搜索失败", e);
        }
    }

    public String createSsoBridgeCode(RagSsoLoginReq request) {
        try {
            HttpResponse<String> response = post("/api/v1/integration/sso/login", request);
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");
            String bridgeCode = data.path("bridgeCode").asText();
            if (bridgeCode == null || bridgeCode.isBlank()) {
                throw new RuntimeException("RAG 登录桥接码为空");
            }
            return bridgeCode;
        } catch (Exception e) {
            throw new RuntimeException("调用 RAG 登录桥接失败", e);
        }
    }

    private void postWithoutResponse(String path, Object request) {
        try {
            HttpResponse<String> response = post(path, request);
            validateResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("调用 RAG 接口失败", e);
        }
    }

    private HttpResponse<String> post(String path, Object request) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(request);
        HttpRequest httpRequest = baseRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return response;
    }

    private HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("X-Internal-Token", internalToken)
                .timeout(Duration.ofSeconds(60));
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        log.error("RAG 接口调用失败, status={}, body={}", response.statusCode(), response.body());
        throw new RuntimeException("RAG 接口调用失败: HTTP " + response.statusCode());
    }
}

package com.github.paicoding.forum.service.chatv2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.chatv2.ModelInfoVO;
import com.github.paicoding.forum.service.chatv2.repository.entity.ChatMessageDO;
import com.github.paicoding.forum.service.rag.RagChatHistoryItem;
import com.github.paicoding.forum.service.rag.RagChatRecommendation;
import com.github.paicoding.forum.service.rag.RagChatReq;
import com.github.paicoding.forum.service.rag.RagChatResp;
import com.github.paicoding.forum.service.rag.RagClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagAssistantService {

    public static final String SMART_ASSISTANT_MODEL_ID = "smart-assistant";

    private final RagClient ragClient;
    private final ChatConversationService chatConversationService;
    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Flux<String> executeStreamingChat(Long historyId, Long userId, String modelId, String userName, String userMessage) {
        chatConversationService.updateConversationTime(historyId);

        List<RagChatHistoryItem> history = chatMessageService.getRecentMessages(historyId, 20).stream()
                .map(this::toHistoryItem)
                .toList();

        chatMessageService.saveMessage(historyId, "user", userMessage, null);

        return Flux.defer(() -> {
                    RagChatResp response = ragClient.chat(new RagChatReq(
                            userId,
                            userName,
                            String.valueOf(historyId),
                            userMessage,
                            history,
                            5
                    ));

                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("recommendations", response.recommendations());
                    metadata.put("references", response.references());
                    chatMessageService.saveMessage(historyId, "assistant", response.answer(), metadata);

                    List<String> chunks = splitMessage(response.answer());
                    Flux<String> contentStream = Flux.fromIterable(chunks)
                            .delayElements(Duration.ofMillis(15));
                    return contentStream.concatWith(Flux.just("[DONE]"));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(error -> {
                    log.error("智能助手问答失败, historyId={}", historyId, error);
                    String fallbackMessage = "智能助手暂时不可用，请稍后重试。";
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("error", true);
                    metadata.put("errorMessage", error.getMessage());
                    chatMessageService.saveMessage(historyId, "assistant", fallbackMessage, metadata);
                    return Flux.fromIterable(splitMessage(fallbackMessage))
                            .delayElements(Duration.ofMillis(15))
                            .concatWith(Flux.just("[DONE]"));
                });
    }

    public List<ModelInfoVO> getModels() {
        ModelInfoVO model = new ModelInfoVO();
        model.setId(SMART_ASSISTANT_MODEL_ID);
        model.setName("智能助手");
        model.setProvider("sxh-rag");
        model.setDescription("基于社区文章语料的 RAG 智能问答");
        model.setEnabled(true);
        model.setMaxTokens(4096);
        model.setTemperature(0.3);
        return List.of(model);
    }

    public String getDefaultModel() {
        return SMART_ASSISTANT_MODEL_ID;
    }

    private RagChatHistoryItem toHistoryItem(ChatMessageDO messageDO) {
        return new RagChatHistoryItem(messageDO.getRole(), messageDO.getContent());
    }

    private List<String> splitMessage(String message) {
        if (message == null || message.isBlank()) {
            return List.of("");
        }

        int step = 24;
        java.util.ArrayList<String> chunks = new java.util.ArrayList<>();
        for (int i = 0; i < message.length(); i += step) {
            chunks.add(message.substring(i, Math.min(i + step, message.length())));
        }
        return chunks;
    }
}

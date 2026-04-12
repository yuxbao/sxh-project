package com.github.sxh.forum.web.controller.chatv2;

import com.github.sxh.forum.api.model.context.ReqInfoContext;
import com.github.sxh.forum.api.model.vo.ResVo;
import com.github.sxh.forum.api.model.vo.chatv2.*;
import com.github.sxh.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.sxh.forum.core.permission.Permission;
import com.github.sxh.forum.core.permission.UserRole;
import com.github.sxh.forum.service.chatv2.repository.entity.ChatHistoryDO;
import com.github.sxh.forum.service.chatv2.repository.entity.ChatMessageDO;
import com.github.sxh.forum.service.chatv2.service.ChatConversationService;
import com.github.sxh.forum.service.chatv2.service.ChatMessageService;
import com.github.sxh.forum.service.chatv2.service.RagAssistantService;
import com.github.sxh.forum.service.rag.RagClient;
import com.github.sxh.forum.service.rag.RagSsoLoginReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chat V2 REST 控制器
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/chatv2/api")
@RequiredArgsConstructor
public class ChatV2RestController {

    private final ChatConversationService conversationService;
    private final ChatMessageService messageService;
    private final RagAssistantService ragAssistantService;
    private final RagClient ragClient;

    @Value("${sxh.rag.frontend-url:http://localhost:9527}")
    private String ragFrontendUrl;

    @Value("${sxh.rag.frontend-hash-mode:true}")
    private boolean ragFrontendHashMode;

    /**
     * 发送消息（流式响应）
     * 参考 deepextract 实现：使用 TEXT_PLAIN 避免 Spring 自动应用 SSE 格式
     * 因为 PaiWebConfig 中配置了 TEXT_EVENT_STREAM 作为默认类型，需要显式指定为纯文本
     */
    @PostMapping(value = "/send", produces = MediaType.TEXT_PLAIN_VALUE)
    @Permission(role = UserRole.LOGIN)
    public Flux<String> sendMessage(@RequestBody ChatSendReqVO request) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();

        // 验证 conversationId
        if (request.getConversationId() == null || request.getConversationId().isEmpty()) {
            return Flux.just("[ERROR] conversationId is required", "[DONE]");
        }

        // 获取或创建会话（通过 conversationId 查找）
        ChatHistoryDO history = conversationService.getConversationByConversationId(request.getConversationId(), userId);
        Long historyId;

        if (history == null) {
            // 首次对话，创建新会话
            String modelId = request.getModelId();
            if (modelId == null || modelId.isEmpty()) {
                modelId = ragAssistantService.getDefaultModel();
            }

            history = conversationService.createConversationWithConversationId(userId, modelId, request.getConversationId());
            historyId = history.getId();

            // 自动生成标题
            if (request.getMessage() != null && !request.getMessage().isEmpty()) {
                conversationService.generateTitleFromMessage(historyId, request.getMessage());
            }
        } else {
            // 已存在的会话
            historyId = history.getId();
        }

        String modelId = history.getModelName();
        String userName = ReqInfoContext.getReqInfo().getUser() != null
                ? ReqInfoContext.getReqInfo().getUser().getUserName()
                : String.valueOf(userId);

        return ragAssistantService.executeStreamingChat(historyId, userId, modelId, userName, request.getMessage())
                .onErrorResume(error -> {
                    log.error("Error during streaming chat", error);
                    return Flux.just(
                            "[ERROR] " + error.getMessage(),
                            "[DONE]"
                    );
                });
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping("/conversations")
    @Permission(role = UserRole.LOGIN)
    public ResVo<List<ConversationVO>> getConversations() {
        Long userId = ReqInfoContext.getReqInfo().getUserId();

        List<ChatHistoryDO> histories = conversationService.getConversationsByUser(userId);

        List<ConversationVO> conversations = histories.stream()
                .map(this::convertToConversationVO)
                .collect(Collectors.toList());

        return ResVo.ok(conversations);
    }

    /**
     * 获取会话详情（包含消息）
     */
    @GetMapping("/conversation/{conversationId}")
    @Permission(role = UserRole.LOGIN)
    public ResVo<ConversationVO> getConversation(@PathVariable String conversationId) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();

        ChatHistoryDO history = conversationService.getConversationByConversationId(conversationId, userId);
        if (history == null) {
            return ResVo.fail(com.github.sxh.forum.api.model.vo.constants.StatusEnum.FORBID_ERROR_MIXED,
                "Conversation not found or unauthorized");
        }

        // 获取消息列表
        List<ChatMessageDO> messages = messageService.getMessagesByHistoryId(history.getId());

        ConversationVO conversationVO = convertToConversationVO(history);
        conversationVO.setMessages(messages.stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList()));

        return ResVo.ok(conversationVO);
    }


    /**
     * 更新会话标题
     */
    @PutMapping("/conversation/{conversationId}/title")
    @Permission(role = UserRole.LOGIN)
    public ResVo<Void> updateTitle(@PathVariable String conversationId, @RequestBody UpdateTitleReqVO request) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();

        // 使用 conversationId 更新标题
        boolean success = conversationService.updateTitleByConversationId(conversationId, userId, request.getTitle(), "user");

        if (success) {
            return ResVo.ok(null);
        } else {
            return ResVo.fail(com.github.sxh.forum.api.model.vo.constants.StatusEnum.FORBID_ERROR_MIXED,
                "Conversation not found or unauthorized");
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversation/{conversationId}")
    @Permission(role = UserRole.LOGIN)
    public ResVo<Void> deleteConversation(@PathVariable String conversationId) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();

        boolean success = conversationService.deleteConversationByConversationId(conversationId, userId);

        if (success) {
            return ResVo.ok(null);
        } else {
            return ResVo.fail(com.github.sxh.forum.api.model.vo.constants.StatusEnum.UNEXPECT_ERROR,
                "Failed to delete conversation");
        }
    }

    /**
     * 获取可用模型列表
     */
    @GetMapping("/models")
    public ResVo<List<ModelInfoVO>> getModels() {
        return ResVo.ok(ragAssistantService.getModels());
    }

    /**
     * 获取默认模型
     */
    @GetMapping("/models/default")
    public ResVo<String> getDefaultModel() {
        return ResVo.ok(ragAssistantService.getDefaultModel());
    }

    /**
     * 生成新的 conversationId (UUID)
     */
    @PostMapping("/conversation/generate-id")
    @Permission(role = UserRole.LOGIN)
    public ResVo<String> generateConversationId() {
        String conversationId = java.util.UUID.randomUUID().toString();
        log.info("Generated new conversationId: {}", conversationId);
        return ResVo.ok(conversationId);
    }

    @GetMapping("/rag-login-url")
    @Permission(role = UserRole.LOGIN)
    public ResVo<String> getRagLoginUrl() {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();

        String userName = user != null && user.getUserName() != null && !user.getUserName().isBlank()
                ? user.getUserName()
                : String.valueOf(userId);
        String avatar = user != null ? user.getPhoto() : null;
        String role = user != null ? user.getRole() : null;

        String bridgeCode = ragClient.createSsoBridgeCode(new RagSsoLoginReq(
                userId,
                userName,
                userName,
                avatar,
                role
        ));

        String baseUrl = ragFrontendUrl.endsWith("/") ? ragFrontendUrl.substring(0, ragFrontendUrl.length() - 1) : ragFrontendUrl;
        String encodedBridgeCode = URLEncoder.encode(bridgeCode, StandardCharsets.UTF_8);
        String redirectUrl;
        if (ragFrontendHashMode) {
            redirectUrl = baseUrl + "/#/sxh-bridge?code=" + encodedBridgeCode;
        } else {
            redirectUrl = baseUrl + "/sxh-bridge?code=" + encodedBridgeCode;
        }
        return ResVo.ok(redirectUrl);
    }

    // ========== 转换方法 ==========

    private ConversationVO convertToConversationVO(ChatHistoryDO history) {
        ConversationVO vo = new ConversationVO();
        BeanUtils.copyProperties(history, vo);
        return vo;
    }

    private MessageVO convertToMessageVO(ChatMessageDO message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setMetadata(message.getMetadataJson());
        return vo;
    }
}

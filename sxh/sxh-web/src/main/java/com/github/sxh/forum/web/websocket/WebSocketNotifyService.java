package com.github.sxh.forum.web.websocket;

import com.github.sxh.forum.api.model.enums.NotifyTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotifyService {
    private static final String DEFAULT_TYPE = "info";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotifyService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyUser(Long userId, NotifyTypeEnum notifyType, String content) {
        if (userId == null) {
            return;
        }
        String title = notifyType != null ? notifyType.getMsg() : "新消息";
        String body = StringUtils.isNotBlank(content) ? content : "您有新的" + title;
        Integer typeCode = notifyType != null ? notifyType.getType() : null;

        WsNotifyMsgVo payload = new WsNotifyMsgVo(title, body, DEFAULT_TYPE, typeCode);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notify", payload);
    }
}

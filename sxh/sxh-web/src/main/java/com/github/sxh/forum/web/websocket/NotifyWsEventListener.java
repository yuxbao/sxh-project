package com.github.sxh.forum.web.websocket;

import com.github.sxh.forum.api.model.vo.notify.NotifyWsEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class NotifyWsEventListener implements ApplicationListener<NotifyWsEvent> {
    private final WebSocketNotifyService webSocketNotifyService;

    public NotifyWsEventListener(WebSocketNotifyService webSocketNotifyService) {
        this.webSocketNotifyService = webSocketNotifyService;
    }

    @Override
    public void onApplicationEvent(NotifyWsEvent event) {
        webSocketNotifyService.notifyUser(event.getNotifyUserId(), event.getNotifyType(), event.getContent());
    }
}

package com.github.sxh.forum.api.model.vo.notify;

import com.github.sxh.forum.api.model.enums.NotifyTypeEnum;
import org.springframework.context.ApplicationEvent;

public class NotifyWsEvent extends ApplicationEvent {
    private final Long notifyUserId;
    private final NotifyTypeEnum notifyType;
    private final String content;

    public NotifyWsEvent(Object source, Long notifyUserId, NotifyTypeEnum notifyType, String content) {
        super(source);
        this.notifyUserId = notifyUserId;
        this.notifyType = notifyType;
        this.content = content;
    }

    public Long getNotifyUserId() {
        return notifyUserId;
    }

    public NotifyTypeEnum getNotifyType() {
        return notifyType;
    }

    public String getContent() {
        return content;
    }
}

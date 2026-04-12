package com.github.sxh.forum.web.websocket;

public class WsNotifyMsgVo {
    private String title;
    private String content;
    private String type;
    private Integer notifyType;

    public WsNotifyMsgVo() {
    }

    public WsNotifyMsgVo(String title, String content, String type, Integer notifyType) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.notifyType = notifyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(Integer notifyType) {
        this.notifyType = notifyType;
    }
}

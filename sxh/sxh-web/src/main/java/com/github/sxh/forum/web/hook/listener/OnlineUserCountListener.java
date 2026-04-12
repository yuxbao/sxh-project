package com.github.sxh.forum.web.hook.listener;

import com.github.sxh.forum.core.util.SpringUtil;
import com.github.sxh.forum.service.statistics.service.statistic.UserStatisticService;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 通过监听session来实现实时人数统计，暂时不使用
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@WebListener
@Slf4j
public class OnlineUserCountListener implements HttpSessionListener {

    public OnlineUserCountListener() {
        super();
        log.info("【OnlineUserCountListener】init");
    }

    /**
     * 新增session，在线人数统计数+1
     *
     * @param se
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // 监听到session创建
        HttpSessionListener.super.sessionCreated(se);
        SpringUtil.getBean(UserStatisticService.class).incrOnlineUserCnt(1);
    }

    /**
     * session失效，在线人数统计数-1
     *
     * @param se
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSessionListener.super.sessionDestroyed(se);
        SpringUtil.getBean(UserStatisticService.class).incrOnlineUserCnt(-1);
    }
}

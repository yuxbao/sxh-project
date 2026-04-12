package com.github.sxh.forum.service;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author XuYifei
 * @date 2024-07-12
 * <p></p>
 * 加载文章、用户、评论、配置、统计、通知、ai聊天的组件mapper
 */
@Configuration
@ComponentScan("com.github.sxh.forum.service")
@MapperScan(basePackages = {
        "com.github.sxh.forum.service.article.repository.mapper",
        "com.github.sxh.forum.service.user.repository.mapper",
        "com.github.sxh.forum.service.comment.repository.mapper",
        "com.github.sxh.forum.service.config.repository.mapper",
        "com.github.sxh.forum.service.statistics.repository.mapper",
        "com.github.sxh.forum.service.notify.repository.mapper",
        "com.github.sxh.forum.service.chatv2.repository.mapper",})
public class ServiceAutoConfig {

    @PostConstruct
    public void init(){
        System.out.println("成功加载ServiceAutoConfig!");
    }
}

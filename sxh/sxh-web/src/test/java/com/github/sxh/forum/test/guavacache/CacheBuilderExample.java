package com.github.sxh.forum.test.guavacache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CacheBuilderExample {
    public static void main(String[] args) throws ExecutionException {
        // 创建一个 CacheBuilder 对象
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(100)  // 最大缓存条目数
                .expireAfterAccess(30, TimeUnit.MINUTES) // 缓存项在指定时间内没有被访问就过期
                .recordStats();  // 开启统计功能

        // 构建一个 LoadingCache 对象
        LoadingCache<String, String> cache = cacheBuilder.build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return "value：" + key; // 当缓存中没有值时，加载对应的值并返回
            }
        });

        // 存入缓存
        cache.put("itwanger", "沉默王二");

        // 从缓存中获取值
        // put 过
        System.out.println(cache.get("itwanger"));
        // 没 put 过
        System.out.println(cache.get("chenqingyang"));

        // 打印缓存的命中率等统计信息
        System.out.println(cache.stats());
    }

    public void testBuilder() {
        // Guava CacheBuilder可以创建各种自定义配置缓存
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        // 设置最大缓存，超过会触发回收策略
        Cache<Object, Object> cache = cacheBuilder.maximumSize(100)
                // 设置过期时间
                .expireAfterWrite(3, TimeUnit.MINUTES)
                // 设置最大闲置时间
                .expireAfterAccess(2, TimeUnit.HOURS)
                // 开启缓存监控
                .recordStats()
                .build();
    }
}

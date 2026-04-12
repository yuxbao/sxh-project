package com.github.sxh.forum.core.net;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.sxh.forum.core.config.ProxyProperties;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 * <p></p>
 * 当前项目里“代理”的用途主要是为外部 HTTP 调用提供可选的代理出口，典型场景是访问 OpenAI 这类需要代理才能连通的服务。
 */
public class ProxyCenter {

    /**
     * 记录每个source使用的proxy索引
     */
    private static final Cache<String, Integer> HOST_PROXY_INDEX = Caffeine.newBuilder().maximumSize(16).build();
    /**
     * proxy
     */
    private static List<ProxyProperties.ProxyType> PROXIES = new ArrayList<>();


    public static void initProxyPool(List<ProxyProperties.ProxyType> proxyTypes) {
        PROXIES = proxyTypes;
    }

    /**
     * get proxy
     *
     * @return
     */
    static ProxyProperties.ProxyType getProxy(String host) {
        Integer index = HOST_PROXY_INDEX.getIfPresent(host);
        if (index == null) {
            index = -1;
        }
        if (index >= PROXIES.size()) {
            index = 0;
        }
        HOST_PROXY_INDEX.put(host, index);
        return PROXIES.get(index);
    }

    public static Proxy loadProxy(String host) {
        ProxyProperties.ProxyType proxyType = getProxy(host);
        if (proxyType == null) {
            return null;
        }
        return new Proxy(proxyType.getType(), new InetSocketAddress(proxyType.getIp(), proxyType.getPort()));
    }
}

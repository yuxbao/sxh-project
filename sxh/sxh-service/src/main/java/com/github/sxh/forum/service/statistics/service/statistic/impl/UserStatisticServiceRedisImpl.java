package com.github.sxh.forum.service.statistics.service.statistic.impl;

import com.github.sxh.forum.core.cache.RedisClient;
import com.github.sxh.forum.service.statistics.service.statistic.UserStatisticService;

/**
 * @program: sxh
 * @description:
 * @author: XuYifei
 * @create: 2024-10-21
 */
public class UserStatisticServiceRedisImpl implements UserStatisticService {

    private static final String ONLINE_USER_CNT_KEY = "online_user_cnt";

    @Override
    public void incrOnlineUserCnt(int cnt) {
        int onlineUserCnt = getOnlineUserCnt();
        int updated = onlineUserCnt + cnt;
        if (updated < 0) {
            updated = 0;
        }
        RedisClient.setStr(ONLINE_USER_CNT_KEY, String.valueOf(updated));
    }

    @Override
    public int getOnlineUserCnt() {
        int onlineUserCnt = 0;
        try {
            onlineUserCnt = Integer.parseInt(RedisClient.getStr(ONLINE_USER_CNT_KEY));
        } catch (Exception e) {
            RedisClient.setStr(ONLINE_USER_CNT_KEY, "0");
        }
        return onlineUserCnt;
    }

    @Override
    public boolean isOnline(String sessionStr) {
        return false;
    }

    @Override
    public void updateSessionExpireTime(String sessionStr) {
    }
}

package com.github.sxh.forum.core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用常量
 *
 * @author XuYifei
 * @date 2024/07/10
 */
public class CommonConstants {
    private static final String COVER_HOST = "https://r2-storage.yux-bao.site/yuxbao-sixianghui-oss/images/cover/";

    /**
     * 消息队列
     */
    public static String EXCHANGE_NAME_DIRECT = "direct.exchange";
    public static String QUERE_KEY_PRAISE = "praise";
    public static String QUERE_NAME_PRAISE = "quere.praise";
    public static final String MESSAGE_QUEUE_EXCHANGE_NAME_DIRECT = "direct.exchange";
    public static final String MESSAGE_QUEUE_EXCHANGE_NAME_FANOUT = "fanout.exchange";
    public static final String MESSAGE_QUEUE_EXCHANGE_NAME_TOPIC = "topic.exchange";
    public static final String MESSAGE_QUEUE_KEY_NOTIFY = "notify";
    public static final String MESSAGE_QUEUE_KEY_TEST = "test";
    public static final String MESSAGE_QUEUE_NAME_NOTIFY_EVENT = "queue.notify";

    /**
     * 分类类型
     */
    public static final String CATEGORY_ALL             = "全部";
    public static final String CATEGORY_BACK_EMD        = "后端";
    public static final String CATEGORY_FORNT_END       = "前端";
    public static final String CATEGORY_ANDROID         = "Android";
    public static final String CATEGORY_IOS             = "IOS";
    public static final String CATEGORY_BIG_DATA        = "大数据";
    public static final String CATEGORY_INTELLIGENCE    = "人工智能";
    public static final String CATEGORY_CODE_LIFE       = "代码人生";
    public static final String CATEGORY_TOOL            = "开发工具";
    public static final String CATEGORY_READ            = "阅读";

    /**
     * 首页图片
     */
    public static final Map<String, List<String>> HOMEPAGE_TOP_PIC_MAP = new HashMap<String, List<String>>() {
        {
            put(CATEGORY_ALL, coverList("homepage/all-1.jpg", "homepage/all-2.jpg", "homepage/all-3.jpg", "homepage/all-4.jpg"));
            put(CATEGORY_BACK_EMD, coverList("homepage/backend-1.jpg", "homepage/backend-2.jpg", "homepage/backend-3.jpg", "homepage/backend-4.jpg"));
            put(CATEGORY_FORNT_END, coverList("homepage/backend-4.jpg", "homepage/all-1.jpg", "homepage/backend-3.jpg", "homepage/all-3.jpg"));
            put(CATEGORY_ANDROID, coverList("homepage/all-2.jpg", "homepage/backend-4.jpg", "homepage/all-1.jpg", "homepage/backend-2.jpg"));
            put(CATEGORY_IOS, coverList("homepage/all-3.jpg", "homepage/life-1.jpg", "homepage/all-4.jpg", "homepage/read-1.jpg"));
            put(CATEGORY_BIG_DATA, coverList("homepage/backend-2.jpg", "homepage/backend-3.jpg", "homepage/all-1.jpg", "homepage/all-2.jpg"));
            put(CATEGORY_INTELLIGENCE, coverList("homepage/ai-1.jpg", "homepage/ai-2.jpg", "homepage/backend-1.jpg", "homepage/all-1.jpg"));
            put(CATEGORY_CODE_LIFE, coverList("homepage/life-1.jpg", "homepage/life-2.jpg", "homepage/all-3.jpg", "homepage/read-2.jpg"));
            put(CATEGORY_TOOL, coverList("homepage/backend-4.jpg", "homepage/all-3.jpg", "homepage/backend-2.jpg", "homepage/all-1.jpg"));
            put(CATEGORY_READ, coverList("homepage/read-1.jpg", "homepage/read-2.jpg", "homepage/all-4.jpg", "homepage/life-1.jpg"));
        }
    };

    private static List<String> coverList(String... names) {
        List<String> covers = new ArrayList<>();
        for (String name : names) {
            covers.add(COVER_HOST + name);
        }
        return covers;
    }
}

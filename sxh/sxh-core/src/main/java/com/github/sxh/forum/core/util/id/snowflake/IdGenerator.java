package com.github.sxh.forum.core.util.id.snowflake;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface IdGenerator {
    /**
     * 生成分布式id
     *
     * @return
     */
    Long nextId();
}

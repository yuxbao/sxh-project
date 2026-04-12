package com.github.sxh.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.sxh.forum.service.article.repository.entity.ReadCountDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 标签mapper接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ReadCountMapper extends BaseMapper<ReadCountDO> {

    /**
     * 原子性增加阅读计数
     * 
     * @param documentId 文档ID
     * @param documentType 文档类型
     */
    @Update("update read_count set cnt = cnt + 1 where document_id = #{documentId} and document_type = #{documentType}")
    void incrementCount(@Param("documentId") Long documentId, @Param("documentType") Integer documentType);
}

package com.github.paicoding.forum.api.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基本的Data Object数据对象，存储了id和更新创建时间
 */
@Data
public class BaseDO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Date createTime;

    private Date updateTime;
}

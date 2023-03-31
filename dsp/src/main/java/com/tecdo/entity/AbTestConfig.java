package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.common.domain.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型ABTest配置表
 **/
@Data
@TableName("ab_test_config")
@EqualsAndHashCode(callSuper = true)
public class AbTestConfig extends BaseEntity {

    /**
     * tag
     */
    private String tag;

    /**
     * 属性
     */
    private String attribute;

    /**
     * 操作
     */
    private String operation;

    /**
     * 值
     */
    private String value;

    /**
     * 状态
     */
    private Integer status;
}
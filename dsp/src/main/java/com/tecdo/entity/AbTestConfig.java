package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
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
     * 分组,同一个分组的path和weight要求要相等
     */
    private String group;

    /**
     * 目标path
     */
    private String path;

    /**
     * 权重，0-100
     */
    private Double weight;

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
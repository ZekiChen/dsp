package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告定向条件表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("target_condition")
@EqualsAndHashCode(callSuper = true)
public class TargetCondition extends BaseEntity {

    /**
     * 广告组ID
     */
    private Integer adGroupId;

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
}
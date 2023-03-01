package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告推广活动表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("campaign")
@EqualsAndHashCode(callSuper = true)
public class Campaign extends BaseEntity {

    /**
     * campaign名称
     */
    private String name;

    /**
     * 日预算
     */
    private Double dailyBudget;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 分类（多个用逗号分隔）
     */
    private String category;

    /**
     * 推广单子的域名
     */
    private String domain;

    /**
     * 状态
     */
    private Integer status;

}
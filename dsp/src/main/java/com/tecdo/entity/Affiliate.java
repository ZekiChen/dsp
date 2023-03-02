package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 渠道信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("affiliate")
@EqualsAndHashCode(callSuper = true)
public class Affiliate extends BaseEntity {

    /**
     * 渠道名称
     */
    private String name;

    /**
     * 渠道token
     */
    private String secret;

    /**
     * 渠道API
     */
    private String api;

    /**
     * 状态
     */
    private Integer status;

}
package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ip表
 **/
@Data
@TableName("ip_table")
@EqualsAndHashCode(callSuper = true)
public class IpTable extends BaseEntity {

    /**
     * 结束ip
     */
    private String startIp;

    /**
     * 开始ip
     */
    private String endIp;

    /**
     * 类型
     */
    private String type;

}
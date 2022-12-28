package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RTA信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("rta_info")
@EqualsAndHashCode(callSuper = true)
public class RtaInfo extends BaseEntity {

    /**
     * 广告主ID
     */
    private Integer advId;

    /**
     * app key
     */
    private String appKey;

    /**
     * app secret
     */
    private String appSecret;

}
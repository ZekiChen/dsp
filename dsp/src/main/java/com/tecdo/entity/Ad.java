package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("ad")
@EqualsAndHashCode(callSuper = true)
public class Ad extends BaseEntity {

    /**
     * 广告组ID
     */
    private Integer groupId;

    /**
     * 广告名称
     */
    private String name;

    /**
     * 广告类型
     */
    private Integer type;

    /**
     * 图片 creative id
     */
    private Integer image;

    /**
     * icon creative id
     */
    private Integer icon;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * CTA
     */
    private String cta;

    /**
     * 视频 creative id
     */
    private Integer video;

    /**
     * 状态
     */
    private Integer status;

}
package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创意物料表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("creative")
@EqualsAndHashCode(callSuper = true)
public class Creative extends BaseEntity {

    /**
     * 物料名称
     */
    private String name;

    /**
     * 物料类型
     */
    private Integer type;

    /**
     * 物料长度
     */
    private Integer length;

    /**
     * 物料宽度
     */
    private Integer width;

    /**
     * 物料URL
     */
    private String url;

    /**
     * 状态
     */
    private Integer status;

}
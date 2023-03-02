package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.common.domain.entity.BaseEntity;
import com.tecdo.enums.biz.AdTypeEnum;
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
     * @see AdTypeEnum
     */
    private Integer type;

    /**
     * 物料宽度
     */
    private Integer width;

    /**
     * 物料高度
     */
    private Integer height;

    /**
     * 物料URL
     */
    private String url;

    /**
     * 状态
     */
    private Integer status;

}
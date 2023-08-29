package com.tecdo.adm.api.audience.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by Zeki on 2023/6/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_container")
@ApiModel(value="AfContainer对象", description="audience af_container表")
public class AfContainer extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "api_key")
    private String apiKey;

    @ApiModelProperty(value = "广告主调用create接口时，携带的name，表示受众名称")
    private String name;

    @ApiModelProperty(value = "广告主调用create接口时，携带的platform，表示受众信息属于android/ios id")
    private String platform;

    @ApiModelProperty(value = "1：有效；0：无效")
    private Boolean isEnable;

    @ApiModelProperty(value = "加密算法")
    private Integer encrypt;

    @ApiModelProperty(value = "设备数")
    private Long deviceCnt;
}

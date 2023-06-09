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
@TableName("af_info")
@ApiModel(value="AfInfo对象", description="audience af_info表")
public class AfInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "af账号唯一标志，可用af的pid来表示")
    private String afPid;

    @ApiModelProperty(value = "af账号下的广告主")
    private String afAdvertiser;

    @ApiModelProperty(value = "af账号下的app id")
    private String afAppId;

    @ApiModelProperty(value = "分配给广告主使用的api_key，分配之后不能修改")
    private String apiKey;

    @ApiModelProperty(value = "广告主分配给我们使用的拉取key，用于替换sync接口中的拉取占位符")
    private String pullKey;

    @ApiModelProperty(value = "1：有效；0：无效")
    private Boolean isEnable;

}

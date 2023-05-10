package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author sisyphus
 * @since 2023-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_sync")
@ApiModel(value="AfSync对象", description="audience af_sync表")
public class AfSync implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "container_id")
    private Integer containerId;

    @ApiModelProperty(value = "url")
    private String url;

    @ApiModelProperty(value = "url_hashed")
    private String urlHashed;

    @ApiModelProperty(value = "url_adid_sha256")
    private String urlAdidSha256;

    @ApiModelProperty(value = "url_email_sha256")
    private String urlEmailSha256;

    @ApiModelProperty(value = "url_phone_number_sha256")
    private String urlPhoneNumberSha256;

    @ApiModelProperty(value = "这次下发记录的时间戳，用于版本比较")
    private Long versionMillis;

    @ApiModelProperty(value = "1:已经同步到redis;0: 还没有同步")
    private Boolean hasSync;

    @ApiModelProperty(value = "init：未进行初始化，可以分配任务；running：正在进行初始化，有任务在执行；finish：执行完毕，不再需要更新")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "1：有效；0：无效")
    private Boolean isEnable;


}

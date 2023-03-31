package com.tecdo.starter.mp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tecdo.starter.mp.util.MpDateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 基础实体：主键ID + 创建时间 + 更新时间
 *
 * Created by Zeki on 2022/12/26
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseEntity extends IdEntity {

    @DateTimeFormat(pattern = MpDateUtil.PATTERN_DATETIME)
    @JsonFormat(pattern = MpDateUtil.PATTERN_DATETIME)
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @DateTimeFormat(pattern = MpDateUtil.PATTERN_DATETIME)
    @JsonFormat(pattern = MpDateUtil.PATTERN_DATETIME)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

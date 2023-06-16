package com.tecdo.adm.api.log.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.mp.util.MpDateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 业务接口日志表
 *
 * Created by Zeki on 2023/6/14
 */
@Data
@TableName("biz_log_api")
@EqualsAndHashCode(callSuper = true)
public class BizLogApi extends IdEntity {

	private static final long serialVersionUID = 1L;

    /**
     * 业务表主键
     */
    private Integer bizId;
	/**
	 * 操作类型
	 * @see com.tecdo.adm.api.log.enums.OptTypeEnum
	 */
	private Integer optType;
	/**
	 * 业务类型
     * @see com.tecdo.adm.api.log.enums.BizTypeEnum
	 */
	private Integer bizType;
	/**
	 * 日志标题
	 */
	private String title;
	/**
	 * 日志内容
	 */
	private String content;
	/**
	 * 创建人
	 */
	private String creator;
	/**
	 * 创建时间
	 */
	@DateTimeFormat(pattern = MpDateUtil.PATTERN_DATETIME)
	@JsonFormat(pattern = MpDateUtil.PATTERN_DATETIME)
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;

}

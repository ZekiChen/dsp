package com.tecdo.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 报表与渠道gap差异
 *
 * Created by Zeki on 2023/5/29
 */
@Data
@TableName("report_aff_gap")
@EqualsAndHashCode(callSuper = true)
public class ReportAffGap extends IdEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建日期
	 */
	private Date createDate;
	/**
	 * 渠道ID
	 */
	private Integer affId;
	/**
	 * 渠道曝光量
	 */
	private Long affImp;
	/**
	 * 报表曝光量
	 */
	private Long dspImp;
	/**
	 * 曝光量gap百分比
	 */
	private Double gapImp;
	/**
	 * 渠道花费
	 */
	private Double affCost;
	/**
	 * 报表花费
	 */
	private Double dspCost;
	/**
	 * 花费gap百分比
	 */
	private Double gapCost;

}

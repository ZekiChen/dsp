package com.tecdo.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表与广告主gap差异
 *
 * Created by Zeki on 2023/5/29
 */
@Data
@TableName("report_adv_gap")
@EqualsAndHashCode(callSuper = true)
public class ReportAdvGap extends IdEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建日期
	 */
	private String createDate;
	/**
	 * 广告主ID
	 */
	private Integer advId;
	/**
	 * 国家二字码
	 */
	private String country;
	/**
	 * 广告主event1数量
	 */
	private Long advEvent1;
	/**
	 * 报表event1数量
	 */
	private Long dspEvent1;
	/**
	 * event1 gap
	 */
	private Double event1Gap;
	/**
	 * 广告主event2数量
	 */
	private Long advEvent2;
	/**
	 * 报表event2数量
	 */
	private Long dspEvent2;
	/**
	 * event2 gap
	 */
	private Double event2Gap;
	/**
	 * 广告主event3数量
	 */
	private Long advEvent3;
	/**
	 * 报表event3数量
	 */
	private Long dspEvent3;
	/**
	 * event3 gap
	 */
	private Double event3Gap;
}

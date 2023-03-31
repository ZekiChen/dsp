package com.tecdo.starter.mp.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Zeki on 2023/3/13
 */
@Data
@ConfigurationProperties(prefix = "pac.mybatis-plus")
public class MPProperties {

	/**
	 * 溢出总页数后是否进行处理
	 */
	protected Boolean overflow = false;

	/**
	 * join优化
	 */
	private Boolean optimizeJoin = false;

}

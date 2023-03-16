package com.tecdo.starter.oss.rule;

/**
 * Created by Zeki on 2023/3/13
 */
public interface OssRule {

	/**
	 * 获取文件名规则
	 *
	 * @param originalFilename 文件名
	 * @return String
	 */
	String fileName(String originalFilename);

}

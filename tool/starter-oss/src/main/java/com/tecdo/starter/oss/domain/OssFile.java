package com.tecdo.starter.oss.domain;

import lombok.Data;

import java.util.Date;

/**
 * Created by Zeki on 2023/3/13
 */
@Data
public class OssFile {

	private String url;
	private String name;
	public String hash;
	private long length;
	private Date uploadTime;
	private String contentType;
}

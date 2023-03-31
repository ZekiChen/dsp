package com.tecdo.starter.oss.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Zeki on 2023/3/13
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

	private String accessKey = "POY9RYLUH5JTN7FBXLJO";
	private String secretKey = "bXsvMvC30FarkEt9tyxrDFWeVRENgwlqtDgsm9Gm";

	private String endpoint = "https://obs.ap-southeast-3.myhuaweicloud.com";
	private String bucketName = "pac-dsp-material";

	private String cdnUrl = "https://dsp-material.tec-do.cn";
}

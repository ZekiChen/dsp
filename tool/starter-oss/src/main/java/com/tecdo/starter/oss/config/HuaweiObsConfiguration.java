package com.tecdo.starter.oss.config;

import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.tecdo.starter.oss.HuaweiObsTemplate;
import com.tecdo.starter.oss.props.OssProperties;
import com.tecdo.starter.oss.rule.OssRule;
import com.tecdo.starter.oss.rule.PacOssRule;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Created by Zeki on 2023/3/13
 */
@AllArgsConstructor
@AutoConfiguration
@ConditionalOnClass(ObsClient.class)
@EnableConfigurationProperties(OssProperties.class)
public class HuaweiObsConfiguration {

	private final OssProperties ossProperties;

	@Bean
	@ConditionalOnMissingBean(OssRule.class)
	public OssRule ossRule() {
		return new PacOssRule();
	}

	@Bean
	@ConditionalOnMissingBean(ObsClient.class)
	public ObsClient ossClient() {
		// 使用可定制各参数的配置类（ObsConfiguration）创建OBS客户端（ObsClient），创建完成后不支持再次修改参数
		ObsConfiguration conf = new ObsConfiguration ();

		conf.setEndPoint(ossProperties.getEndpoint());
		// 设置OSSClient允许打开的最大HTTP连接数，默认为1024个。
		conf.setMaxConnections(1024);
		// 设置Socket层传输数据的超时时间，默认为50000毫秒。
		conf.setSocketTimeout(50000);
		// 设置建立连接的超时时间，默认为50000毫秒。
		conf.setConnectionTimeout(50000);
		// 设置从连接池中获取连接的超时时间（单位：毫秒），默认不超时。
		conf.setConnectionRequestTimeout(1000);
		// 设置连接空闲超时时间。超时则关闭连接，默认为60000毫秒。
		conf.setIdleConnectionTime(60000);
		// 设置失败请求重试次数，默认为3次。
		conf.setMaxErrorRetry(5);

		return new ObsClient(ossProperties.getAccessKey(), ossProperties.getSecretKey(), conf);
	}

	@Bean
	@Primary
	@ConditionalOnBean({ObsClient.class, OssRule.class})
	public HuaweiObsTemplate huaweiobsTemplate(ObsClient ossClient, OssRule ossRule) {
		return new HuaweiObsTemplate(ossClient, ossProperties, ossRule);
	}
}

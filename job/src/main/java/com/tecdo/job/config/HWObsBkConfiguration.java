package com.tecdo.job.config;

import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.tecdo.job.foreign.pixalate.PixalatePostBidRule;
import com.tecdo.starter.oss.HuaweiObsTemplate;
import com.tecdo.starter.oss.props.OssProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Elwin on 2024/1/24
 */
@Configuration
@ConditionalOnClass(ObsClient.class)
public class HWObsBkConfiguration {

    @Bean(name = "hwobsbkTemplate")
    public HuaweiObsTemplate hwobsbkTemplate() {
        OssProperties ossProperties = new OssProperties();
        ossProperties.setAccessKey("TZPSS9LDC5NHXTYW6CZW");
        ossProperties.setSecretKey("GPDw7rI0EsZd20U7d74Ig0ARrt55MDNaDbccRT4V");
        ossProperties.setEndpoint("https://obs.ap-southeast-3.myhuaweicloud.com");
        ossProperties.setBucketName("hwsg-pacdsp-bk-prod-01");

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

        ObsClient obsClient = new ObsClient(ossProperties.getAccessKey(), ossProperties.getSecretKey(), conf);
        PixalatePostBidRule ossRule = new PixalatePostBidRule();

        return new HuaweiObsTemplate(obsClient, ossProperties, ossRule);
    }
}

package com.tecdo.service.track;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;

import java.util.List;

/**
 * 追踪链构建器
 *
 * Created by Zeki on 2023/9/14
 */
public interface ITrackBuilder {

    /**
     * 构建追踪链
     * @param sysTrack 系统默认追踪链
     * @param wrapper 广告单元
     * @param sign 通知密钥
     * @param bidRequest 竞价请求信息
     * @param affiliate 渠道信息
     * @return 系统默认追踪链 + adGroup配置的追踪链
     */
    List<String> build(String sysTrack, AdDTOWrapper wrapper, String sign,
                       BidRequest bidRequest, Affiliate affiliate);
}

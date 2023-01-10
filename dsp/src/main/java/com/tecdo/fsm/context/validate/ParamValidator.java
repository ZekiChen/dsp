package com.tecdo.fsm.context.validate;

import com.tecdo.domain.openrtb.request.BidRequest;

/**
 * BidRequest 参数校验器
 *
 * Created by Zeki on 2023/1/10
 */
public class ParamValidator {

    /**
     * 必填项前置校验（先面向过程Coding，后面可以优化）
     *
     * @param bidRequest 竞价请求对象
     * @return         true：校验通过
     */
    public static boolean validate(BidRequest bidRequest) {
        // 目标渠道：目前只参与移动端流量的竞价
        if (bidRequest.getApp() == null) {
            return false;
        }
        // 设备信息都不传，也不太合理
        if (bidRequest.getDevice() == null) {
            return false;
        }
        return true;
    }
}

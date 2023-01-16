package com.tecdo.fsm.context.validate;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;

import java.util.List;

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
        // 设备信息都不传，不太合理
        if (bidRequest.getDevice() == null) {
            return false;
        }
        // 展示位必须有
        List<Imp> imp = bidRequest.getImp();
        if (CollUtil.isEmpty(imp)) {
            return false;
        }
        // 展示位底价必须有
        if (imp.stream().anyMatch(e -> e.getBidfloor() == null)) {
            return false;
        }

        return true;
    }
}

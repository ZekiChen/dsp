package com.tecdo.domain.biz.log;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * BidRequest 参数校验通过后，记录竞价请求日志
 * <p>
 * Created by Zeki on 2023/1/31
 */
@Builder
public class RequestLog implements Serializable {

    /**
     * 日志产生的时间，精确到小时 yyyy-MM-dd_HH
     */
    private String createTime;

    /**
     * 后13位为unix时间戳
     */
    private String bidId;

    /**
     * 交易所id，也是渠道id
     */
    private Integer affiliateId;

    /**
     * 交易所名字
     */
    private String affiliateName;

    /**
     * 流量类型，banner，native，video
     */
    private String adFormat;

    /**
     * 版位宽度
     */
    private String adWidth;

    /**
     * 版位高度
     */
    private String adHeight;

    /**
     * Android/IOS
     */
    private String os;

    /**
     * 设备厂商
     */
    private String deviceMake;

    /**
     * 流量来源媒体
     */
    private String bundleId;

    /**
     * 国家
     */
    private String country;

    /**
     * 网络连接类型
     */
    private Integer connectionType;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 系统版本
     */
    private String osv;

    /**
     * 运营商
     */
    private String carrier;

    /**
     * 广告所处位置
     */
    private Integer pos;

    /**
     * 广告是否为插屏/全屏
     */
    private Integer instl;

    /**
     * 当前bundle的域名
     */
    private String domain;

    /**
     * 当前bundle所属类别
     */
    private List<String> cat;

    /**
     * 设备ip
     */
    private String ip;

    /**
     * 设备ua
     */
    private String ua;

    /**
     * 设备语言
     */
    private String lang;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 竞价底价
     */
    private Double bidFloor;
}

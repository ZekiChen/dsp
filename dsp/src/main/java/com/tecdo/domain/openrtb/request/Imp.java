package com.tecdo.domain.openrtb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tecdo.domain.openrtb.base.Extension;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 一个广告位或者将要参与竞拍的展示信息。
 * 一个竞价请求可以包含多个 Imp 对象，这种状况的一个示例是 ADX 支持售卖一个页面的所有广告位。
 * 为了便于竞拍者区分， 每一个Imp对象都要有一个唯一标识（ID).
 *
 * Created by Zeki on 2022/12/22
 **/
@Setter
@Getter
public class Imp extends Extension {

    /**
     * 在当前竞价请求上下文中唯一标识本次展示的标识（必须，通常从 1 开始并以此递增）
     */
    private String id;

    /**
     * 度量指标
     */
    private List<Metric> metric;

    /**
     * 如果展示需要以 横幅 的形式提供则需要填充
     */
    private Banner banner;

    /**
     * 如果展示需要以 视频 的形式提供则需要填充
     */
    private Video video;

    /**
     * 如果展示需要以 音频 的形式提供则需要填充
     */
    private Audio audio;

    /**
     * 如果展示需要以 原生广告 的形式提供则需要填充
     */
    @JsonProperty(value = "native")
    private Native native1;

    /**
     * 包含对本次展示生效的任何私有市场交易
     */
    private Pmp pmp;

    /**
     * 广告媒体合作伙伴的名字，用于渲染广告的SDK技术或者播放器（通常是视频或者移动广告）
     * 某些广告服务需要根据合作伙伴定制广告代码，推荐在视频广告或应用广告中填充
     */
    private String displaymanager;

    /**
     * 广告媒体合作伙伴，用于渲染广告的SDK技术或者播放器（通常是视频或者移动广告）的版本号
     * 某些广告服务需要根据合作伙伴定制广告代码，推荐在视频广告或应用广告中填充
     */
    private String displaymanagerver;

    /**
     * 0-不是插屏广告； 1-广告是插屏或者全屏广告
     */
    private Integer instl = 0;

    /**
     * 某个特定广告位或者广告标签的标识，用于发起竞拍。为了方便调试问题或者进行买方优化
     */
    private String tagid;

    /**
     * 本次展示的最低竞拍价格，以 CPM 表示
     */
    private Float bidfloor = 0F;

    /**
     * ISO-4217 规定的字母码表标识的货币类型。如果 ADX 允许，可能与从竞拍者返回的竞价货币不同。
     */
    private String bidfloorcur = "USD";

    /**
     * 在应用程序中单击创意时打开的浏览器类型
     * 0-嵌入式； 1-原生
     * 注意 iOS 9.x 设备中的 Safari 视图控制器被认为是原生浏览器
     */
    private Integer clickbrowser;

    /**
     * 标识展示请求是否需要使用 HTTPS 加密物料信息以及markup以保证安全
     * 0-不需要使用安全链路； 1-需要使用安全链路； 如果不填充，则表示未知，可以认为是不需要使用安全链路
     */
    private Integer secure;

    /**
     * 特定交易支持的 iframe buster 的名字数组
     */
    private List<String> iframebuster;

    /**
     * 关于拍卖和实际展示之间可能经过的秒数的咨询
     */
    private Integer exp;

    /**
     * VIVO自定义协议
     * 曝光类型：1-native；其他类型后续支持
     */
    private Integer impType;

    /**
     * VIVO自定义协议
     * 本次展示的最低竞拍价格，cpm出价，单位美分，仅仅支持整数
     */
    private Float bidFloor = 0F;

    /**
     * Imp对象存在 bidfloor（标准协议）和 bidFloor（VIVO自定义协议），这里如果不加下面两个 getter 会导致编译不通过
     */
    public Float getBidfloor() {
        return bidfloor;
    }
    public Float getBidFloor() {
        return bidFloor;
    }
}

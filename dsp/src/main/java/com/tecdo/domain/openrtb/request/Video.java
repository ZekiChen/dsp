package com.tecdo.domain.openrtb.request;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 视频广告
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Video extends Extension {

    /**
     * 支持的内容mime-type，例如 “video/x-ms-wmv”, “video/mp4”（必须）
     */
    private List<String> mimes;

    /**
     * 最小的视频长度，以秒为单位（推荐）
     */
    private Integer minduration;

    /**
     * 最大的视频长度，以秒为单位（推荐）
     */
    private Integer maxduration;

    /**
     * 支持的视频竞价响应协议数组（至少指定一个支持的协议）
     * @see ProtocolsEnum
     */
    private List<Integer> protocols;

    /**
     * 视频播放器的宽度，像素为单位（推荐）
     */
    private Integer w;

    /**
     * 视频播放器的高度，像素为单位（推荐）
     */
    private Integer h;

    /**
     * 视频前，中及之后的广告位中视频广告的启动延时，以秒为单位（推荐）
     * @see StartDelayEnum
     */
    private Integer startdelay;

    /**
     * 展示的放置类型
     * @see VideoPlacementTypeEnum
     */
    private Integer placement;

    /**
     * 展示是否必须是线性的，如果没有指定，则标识都是被允许的
     * @see VideoLinearityEnum
     */
    private Integer linearity;

    /**
     * 是否允许视频广告可以被跳过
     * 0-不允许； 1-允许
     * 如果竞价者发送的标记/创意本身是可跳过的，则Bid对象应该包含attr数组，其中元素为16表示可跳过的视频。
     * @see CreativeAttributeEnum#SKIP_BUTTON
     */
    private Integer skip;

    /**
     * 总持续时间大于此秒数的视频可以跳过；仅适用于AD是可跳过的
     */
    private Integer skipmin = 0;

    /**
     * 启用跳过之前视频必须播放的秒数；仅适用于AD是可跳过的
     */
    private Integer skipafter = 0;

    /**
     * 如果在同一个竞价请求中提供了多个展示，则需要考虑多个物料传输的顺序
     */
    private Integer sequence;

    /**
     * 限制的物料属性
     * @see CreativeAttributeEnum
     */
    private List<Integer> battr;

    /**
     * 最大的视频广告延长时间长度（如果支持延长）
     * 为空或者0：不允许延长； -1：允许延时，且没有时间限制； >0：可以延长的时间长度比maxduration大的值
     */
    private Integer maxextended;

    /**
     * 最小的比特率，以 Kbps 为单位。交易平台可以动态的设置这个值或者为所有发布者统一设置该值
     */
    private Integer minbitrate;

    /**
     * 最大的比特率，以 Kbps 为单位。交易平台可以动态的设置这个值或者为所有发布者统一设置该值
     */
    private Integer maxbitrate;

    /**
     * 是否允许将 4：3 的内容展示在 16：9 的窗口
     * 0-不允许； 1-允许
     */
    private Integer boxingallowed = 1;

    /**
     * 允许的回放方式，如果没有指定，表示支持全部
     * @see PlaybackMethodEnum
     */
    private List<Integer> playbackmethod;

    /**
     * 导致回放结束的事件
     * @see PlaybackCessationModeEnum
     */
    private Integer playbackend;

    /**
     * 支持的传输方式（例如流式传输，逐步传输），如果没有指定，表示全部支持
     * @see ContentDeliveryMethodEnum
     */
    private List<Integer> delivery;

    /**
     * 广告在屏幕上的位置
     * @see AdPositionEnum
     */
    private Integer pos = 0;

    /**
     * 如果支持复合广告，表示一组Banner对象
     */
    private List<Banner> companionad;

    /**
     * 本次展示支持的API框架列表，如果一个API没有被显式在列表中指明，则表示不支持
     * @see APIFrameworkEnum
     */
    private List<Integer> api;

    /**
     * 支持的 VAST companion 广告类型。如果在 companionad 中填充了Banner对象则推荐使用
     * @see CompanionTypeEnum
     */
    private List<Integer> companiontype;

}

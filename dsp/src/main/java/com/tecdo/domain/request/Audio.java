package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import com.tecdo.enums.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 音频广告
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Audio extends Extension {

    /**
     * 支持的内容mime-type，例如 “audio/mp4”（必须）
     */
    private List<String> mimes;

    /**
     * 最小音频广告持续时间(以秒为单位)（推荐）
     */
    private Integer minduration;

    /**
     * 最大音频广告持续时间(以秒为单位)（推荐）
     */
    private Integer maxduration;

    /**
     * 支持的音频广告协议（推荐）
     * @see ProtocolsEnum
     */
    private List<Integer> protocols;

    /**
     * 滚动前、滚动中或滚动后广告放置的开始延迟(以秒为单位)（推荐）
     */
    private Integer startdelay;

    /**
     * 如果在同一个竞价请求中提供多个广告展示，序列号将允许多个创意的协调交付
     */
    private Integer sequence;

    /**
     * 限制的物料属性
     * @see CreativeAttributeEnum
     */
    private List<Integer> battr;

    /**
     * 最大的广告延长时间长度（如果允许扩展）
     * 空或0：不允许扩展； -1：允许扩展，并且没有时间限制； 大于0：超出maxduration值支持的延长播放的秒数
     */
    private Integer maxextended;

    /**
     * 最小的比特率，以 Kbps 为单位
     */
    private Integer minbitrate;

    /**
     * 最大的比特率，以 Kbps 为单位
     */
    private Integer maxbitrate;

    /**
     * 支持的传输方式（例如流式传输，逐步传输），如果没有指定，表示全部支持
     * @see ContentDeliveryMethodEnum
     */
    private List<Integer> delivery;

    /**
     * 如果支持复合广告，则表示一组Banner对象
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

    /**
     * 在一个广告集中可以播放的广告的最大数量
     */
    private Integer maxseq;

    /**
     * 音频饲料类型
     * @see FeedTypeEnum
     */
    private Integer feed;

    /**
     * 是否广告与音频内容拼接或独立发布
     * 0-否； 1-是
     */
    private Integer stitched;

    /**
     * 体积标准模式
     * @see VolumeNormalizationModeEnum
     */
    private Integer nvol;
}

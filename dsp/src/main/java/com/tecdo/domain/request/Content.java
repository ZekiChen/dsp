package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import com.tecdo.enums.ContentCategoryEnum;
import com.tecdo.enums.ContentContextEnum;
import com.tecdo.enums.IQGMediaRatingsEnum;
import com.tecdo.enums.ProductionQualityEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 内容
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Content extends Extension {

    /**
     * 内容唯一标识
     */
    private String id;

    /**
     * 情节数目（通常用于视频内容）
     */
    private Integer episode;

    /**
     * 内容标题。
     * 视频示例：“Search Committee"(电视）, ”A New Hope"(电影), “Endgame”(为网络制作）
     * 非视频示例：“Why an Antarctic Glacier is Melting So Quickly"(时报杂志文章）
     */
    private String title;

    /**
     * 内容系列。
     * 视频示例：“The Office"(电视）,”Start Wars"(电影,“Arby ‘N’ The Chief”(为网络制作）
     * 非视频示例：“Ecocentric"(时报杂志博客）
     */
    private String series;

    /**
     * 内容季数，通常用于视频内容（例如，“第三季”）
     */
    private String season;

    /**
     * 艺术家与内容
     */
    private String artist;

    /**
     * 最能描述内容的类型(如摇滚、流行等)。
     */
    private String genre;

    /**
     * 内容所属的相册；尤其是音频。
     */
    private String album;

    /**
     * 符合 ISO3901 的国际标准记录代码。
     */
    private String isrc;

    /**
     * 内容提供者的详细信息
     */
    private Producer producer;

    /**
     * 内容的url, 用于买方了解使用的上下文或者审查
     */
    private String url;

    /**
     * 内容生产者的IAB内容类型数组
     * @see ContentCategoryEnum
     */
    private List<String> cat;

    /**
     * 生产质量
     * @see ProductionQualityEnum
     */
    private Integer prodq;

    /**
     * 内容类型（游戏，视频，文本等）
     * @see ContentContextEnum
     */
    private Integer context;

    /**
     * 内容分级（例如， MPAA美国电影分级制度)
     */
    private String contentrating;

    /**
     * 内容的用户评分（比如，星数，点赞数等）
     */
    private String userrating;

    /**
     * 媒体评分，按照QAG规范
     * @see IQGMediaRatingsEnum
     */
    private Integer qagmediarating;

    /**
     * 逗号分隔的内容的关键字信息
     */
    private String keywords;

    /**
     * 0-不是实时； 1-实时
     */
    private Integer livestream;

    /**
     * 0-间接源； 1-直接源
     */
    private Integer sourcerelationship;

    /**
     * 内容长度， 用于音频或者视频
     */
    private Integer len;

    /**
     * 内容语言， 使用ISO-639-1-alpha-2
     */
    private String language;

    /**
     * 表示内容是否可嵌套（例如一个可嵌套的视频播放器）
     * 0-不可以； 1-可以
     */
    private Integer embeddable;

    /**
     * 附加内容数据。每个数据对象表示一个不同的数据源
     */
    private List<com.tecdo.domain.request.Data> data;
}

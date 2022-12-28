package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import com.tecdo.enums.ContentCategoryEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 竞价请求 顶层对象模型
 *
 * Created by Zeki on 2022/12/22
 **/
@Setter
@Getter
public class BidRequest extends Extension {

    /**
     * 每次竞价请求的唯一标识，由 ADX 提供（必须）
     */
    private String id;

    /**
     * 提供的展示信息（必须，至少包含1个imp对象）
     */
    private List<Imp> imp;

    /**
     * 渠道网站（仅仅对 网站 适用且推荐填充）
     */
    private Site site;

    /**
     * 渠道App（仅仅对 App 适用且推荐填充）
     */
    private App app;

    /**
     * 设备信息（推荐）
     */
    private Device device;

    /**
     * 受众信息（推荐）
     */
    private User user;

    /**
     * 测试标识
     * 0-实况（非测试）模式； 1-测试模式（拍卖不计价）
     */
    private Integer test = 0;

    /**
     * 拍卖类型（胜出策略）
     * 1-第一价格； 2-第二价格； 交易特定的拍卖类型可以用大于500的值定义
     */
    private Integer at = 2;

    /**
     * 用于在提交竞价请求时避免超时的最大时间，以毫秒为单位，这个值通常是线下沟通的
     */
    private Integer tmax;

    /**
     * 允许在本次展示上进行竞拍的买家Seat白名单。 ADX和竞拍者必须提前协商好Seat IDs
     */
    private List<String> wseat;

    /**
     * 禁止在本次展示上进行竞拍的买家Seat黑名单。 ADX和竞拍者必须提前协商好Seat IDs
     * 在同一个请求中最多只能使用 wseat 和 bseat 中的一个。省略两者意味着没有座位限制。
     */
    private List<String> bseat;

    /**
     * 用于标识 ADX 是否可以验证当前的展示列表包含了当前上下文中所有展示。
     * 例如，一个页面上的所有广告位，所有的视频广告点（视频前，视频中，时候后）
     * 用于支持路由封锁。 0-不支持或未知； 1-提供的展示列表代表所有可用的展示。
     */
    private Integer allimps = 0;

    /**
     * 本次竞价请求允许的货币列表，使用 ISO-4217 字母码（仅在 ADX 接收多种货币的时候推荐填充）
     */
    private List<String> cur;

    /**
     * 使用 ISO-639-1-alpha-2 的创意人员的语言白名单。省略并不意味着具体的限制，但建议买家考虑设备和/或内容对象(如果可用)中的语言属性。
     */
    private List<String> wlang;

    /**
     * 被封锁的广告主类别，使用 IAB 内容类别
     * @see ContentCategoryEnum
     */
    private List<String> bcat;

    /**
     * 域名封锁列表（比如 ford.com)
     */
    private List<String> badv;

    /**
     * 通过特定于平台的独立于交换的应用程序标识符来阻止应用程序的列表。
     * 在Android上，这些应该是捆绑包或包名(例如，com.foo.mygame)。在iOS上，这些是数字id
     */
    private List<String> bapp;

    /**
     * 提供 关于库存来源和哪个实体做出最终决定 的数据
     */
    private Source source;

    /**
     * 指明对本次请求有效的工业，法律或政府条例
     */
    private Regs regs;

}

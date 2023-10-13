package com.tecdo.domain.openrtb.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.domain.openrtb.response.custom.vivo.VivoTrack;
import com.tecdo.enums.openrtb.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 一个 SeatBid 对象包括一个或者多个 Bid 对象，每一个 Bid 对象通过 impid 关联竞价请求中的一个展示，由一个对该展示出价组成
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bid extends Extension {

    /**
     * 竞拍者生成的竞价ID，用于记录日志或行为追踪（必须）
     */
    private String id;

    /**
     * 关联的竞价请求中的Imp对象的ID（必须）
     */
    private String impid;
    /**
     * vivo自定义
     */
    private String impId;

    /**
     * 虽然本次只是对某一个展示的出价，但是竞拍价格是以CPM表示。
     * 需要注意数据类型是float，所以在处理货币的时候强烈建议使用相关的数学处理对象（比如，Java中的BigDecimal)（必须）
     */
    private Float price;

    /**
     * 胜出通知地址， 如果竞价胜出的时候由交易平台调用； 可选标识serving ad markup
     */
    private String nurl;

    /**
     * 计费通知URL，当中标者根据 ADX 特定的业务策略(例如，通常是交付、查看等)成为计费对象时，由 ADX 调用。可以包括替换宏
     */
    private String burl;

    /**
     * 丢失通知URL，当一个出价已知已经丢失时，由 ADX 调用。可以包括替换宏。
     * 特定于 ADX 的策略可能会排除对损失通知的支持或对获胜清算价格的披露，从而导致${AUCTION_PRICE}宏被删除(即被替换为零长度字符串)
     */
    private String lurl;

    /**
     * 竞拍胜出之后可选的传输ad markup的方式，如果胜出通知中包含ad markup则优先使用adm
     */
    private Object adm;

    /**
     * inmobi专用字段
     */
    private Object admobject;

    /**
     * 预加载的广告ID, 可以在交易胜出的时候使用
     */
    private String adid;

    /**
     * 用于限制检测的广告主域名，对于旋转的物料可以是一个数组，ADX可以限制只允许一个域名
     */
    private List<String> adomain;

    /**
     * 被广告的应用的包名或者其他信息，如果可以，倾向于使用交易内唯一的ID
     */
    private String bundle;

    /**
     * 用于质量或者安全监测的表示广告活动内容的图像地址，不允许缓存
     */
    private String iurl;

    /**
     * Campaign ID ，辅助广告质量检查，iurl代表的一组物料
     */
    private String cid;

    /**
     * Creative ID ，辅助广告质量检查
     */
    private String crid;

    /**
     * 战术ID，使买家能够标记出价，以便向 ADX 报告他们提交出价的战术。策略ID的具体用法和含义应该事先在买方和ADX之间进行沟通
     */
    private String tactic;

    /**
     * creative的IAB内容类型
     * @see ContentCategoryEnum
     */
    private List<String> cat;

    /**
     * 描述creative的属性集合
     * @see CreativeAttributeEnum
     */
    private List<Integer> attr;

    /**
     * 标记所需的API(如果适用)
     * @see APIFrameworkEnum
     */
    private Integer api;

    /**
     * 标记的视频响应协议(如果适用)
     * @see ProtocolsEnum
     */
    private Integer protocol;

    /**
     * 根据IQG指南进行创意媒体评级
     * @see IQGMediaRatingsEnum
     */
    private Integer qagmediarating;

    /**
     * 使用 ISO-639-1-alpha-2 的创意语言。如果创意没有语言内容(例如，只有公司标志的横幅)，也可以使用非标准代码“xx”。
     */
    private String language;

    /**
     * 如果出价从属与某个私有市场直接交易规则，则指向竞价请求中该交易规则的deal.id
     */
    private String dealid;

    /**
     * creative 的宽度，以像素为单位
     */
    private Integer w;

    /**
     * creative 的高度，以像素为单位
     */
    private Integer h;

    /**
     * 当以比例表示尺寸时，创造性的相对宽度。Flex广告必需。
     */
    private Integer wratio;

    /**
     * 当以比例表示尺寸时，创造性的相对高度。Flex广告必需。
     */
    private Integer hratio;

    /**
     * 关于竞标者愿意在拍卖和实际展示之间等待的秒数的咨询。
     */
    private Integer exp;

    /**
     * VIVO自定义协议
     * 监测url
     */
    private List<VivoTrack> trackUrls;

    public String getImpid() {
        return impid;
    }

    public void setImpid(String impid) {
        this.impid = impid;
    }

    public String getImpId() {
        return impId;
    }

    public void setImpId(String impId) {
        this.impId = impId;
    }
}

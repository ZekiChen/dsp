package com.tecdo.domain.openrtb.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.NoBidReasonCodeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 竞价响应 顶层对象模型
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BidResponse extends Extension {

    /**
     * 竞价请求的标识（必须）
     */
    private String id;

    /**
     * 如果出价，则至少应该填充一个
     */
    private List<SeatBid> seatbid;
    /**
     * vivo自定义
     */
    private List<SeatBid> seatBid;

    /**
     * 竞拍者生成的响应ID, 辅助日志或者交易追踪
     */
    private String bidid;

    /**
     * 使用 ISO-4217 码表标识货币类型
     */
    private String cur = "USD";

    /**
     * 可选特性，允许出价者以设置 cookie 的方式向 ADX 传递信息。字符串可以是任何格式，必须使用 base85 编码，JSON编码必须包含转义的引号
     */
    private String customdata;

    /**
     * 不出价的原因
     * @see NoBidReasonCodeEnum
     */
    private Integer nbr;

    public List<SeatBid> getSeatbid() {
        return seatbid;
    }

    public void setSeatbid(List<SeatBid> seatbid) {
        this.seatbid = seatbid;
    }

    public List<SeatBid> getSeatBid() {
        return seatBid;
    }

    public void setSeatBid(List<SeatBid> seatBid) {
        this.seatBid = seatBid;
    }
}

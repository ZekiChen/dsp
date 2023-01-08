package com.tecdo.domain.openrtb.response;

import com.tecdo.domain.openrtb.base.Extension;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 一个竞价响应可以包含多个SeatBid对象，每个代表着不同的出价者且包含一个或多个独立的出价信息。
 * 如果请求中有多个展示信息，group属性可以用来指定一个席位对胜出任何展示感兴趣（可以不是全部展示）或者它仅对胜出所有展示感兴趣
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
public class SeatBid extends Extension {

    /**
     * 每个对象关联一个展示。多个出价可以关联同一个展示（必须，至少包含一个）
     */
    private List<Bid> bid;

    /**
     * 出价者席位标识，代表本次出价的出价人
     */
    private String seat;

    /**
     * 0-展示可以独立胜出； 1-展示必须整组胜出或失败
     */
    private Integer group = 0;

}

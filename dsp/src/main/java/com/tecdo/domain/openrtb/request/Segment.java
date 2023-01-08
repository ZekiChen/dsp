package com.tecdo.domain.openrtb.request;

import com.tecdo.domain.openrtb.base.Extension;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据字段，描述用户信息数据的键值对。
 * 其父对象 Data 是某个给定数据提供者的数据字段的集合。ADX必须优先将字段的名称和值传递给竞拍者。
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
public class Segment extends Extension {

    /**
     * 数据提供者的特定数据段的ID
     */
    private String id;

    /**
     * 数据提供者的特定数据段的名称
     */
    private String name;

    /**
     * 表示数据字段值的字符串
     */
    private String value;
}

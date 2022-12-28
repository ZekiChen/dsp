package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Data 和 Segment 对象一起允许指定用户附加信息。
 * 数据可能来自多个数据源，可能来自 ADX 自身或者第三方提供的信息，可以使用id属性区分。
 * 一个竞价请求可以混合来自多个提供者的数据信息。ADX应该优先提供正在使用的数据提供者的信息。
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
public class Data extends Extension {

    /**
     * 交易特定的数据提供者标识
     */
    private String id;

    /**
     * 交易特定的数据提供者名称
     */
    private String name;

    /**
     * 包含数据内容的一组Segment对象
     */
    private List<Segment> segment;
}

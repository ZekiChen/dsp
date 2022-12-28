package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import com.tecdo.enums.APIFrameworkEnum;
import com.tecdo.enums.CreativeAttributeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 表示一个Native类型的展示。Native广告单元需要无缝的插入其周围的内容中（例如，一个对Twitter或Facebook赞助）。
 * 因此，响应必须具有良好的结构，让展示者能够在细粒度上控制渲染过程。
 *
 * Native小组委员会为OpenRTB开发了一个组合规范，名为Native Ad规范。定义了Native广告的请求参数以及响应结构。
 * 这个对象以字符串的形式提供请求参数，这样的话具体的请求参数就可以按照Native Ad规范独立演进。同样的， 广告markup也会按照该文档指定的结构提供。
 *
 * Native作为Imp的子对象出现表示它是一个具有native类型的展示对象。 同样的展示也可以是一个Banner或者Video广告，只要包含Banner对象或者Video对象。
 * 然而， 任何为展示给定的竞价请求必须符合提供类型中的一个。
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Native extends Extension {


    /**
     * 遵守Native Ad规范的请求体（必须）
     */
    private String request;

    /**
     * Native Ad规范的版本（为了高效解析强烈推荐）
     */
    private String ver;

    /**
     * 本次展示支持的API框架列表。如果一个API没有被显式在列表中指明，则表示不支持
     * @see APIFrameworkEnum
     */
    private List<Integer> api;

    /**
     * 限制的物料属性
     * @see CreativeAttributeEnum
     */
    private List<Integer> battr;
}

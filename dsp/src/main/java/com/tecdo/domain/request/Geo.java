package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import com.tecdo.enums.IPLocationServiceEnum;
import com.tecdo.enums.LocationTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 用于封装一个地理位置信息的多种不同属性。
 * 当作为 Device 对象的子节点的时候，标识设备的地理位置或者用户当前的地理位置。
 * 当作为 User 的子节点的时候，标识用户家的位置（也就是说，不必是用户的当前位置）
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
public class Geo extends Extension {

    /**
     * 纬度信息，取值范围 -90.0 ~ +90.0 ，负值表示南方
     */
    private Float lat;

    /**
     * 经度信息，取值返回 -180.0 ~ +180.0 ，负值表示西方
     */
    private Float lon;

    /**
     * 位置信息的源，当传递lat/lon的时候推荐填充
     * @see LocationTypeEnum
     */
    private Integer type;

    /**
     * 以米为单位的估计定位精度；当指定lat/lon并从设备的位置服务(即type = 1)派生时推荐使用。
     * 请注意，这是设备报告的精度。参考操作系统特定的文档(例如，Android, iOS)以获得准确的解释。
     */
    private Integer accuracy;

    /**
     * 自建立此地理位置修复以来的秒数。注意，设备可能会跨多个读取缓存位置数据。理想情况下，这个值应该是实际修复的时间。
     */
    private Integer lastfix;

    /**
     * 用于从IP地址确定地理位置(如适用)的服务或提供程序(即type = 2)
     * @see IPLocationServiceEnum
     */
    private Integer ipservice;

    /**
     * 国家码，使用 ISO-3166-1-alpha-3
     */
    private String country;

    /**
     * 区域码，使用 ISO-3166-2 ；如果美国则使用2字母区域码
     */
    private String region;

    /**
     * 国家的区域，使用 FIPS 10-4 表示。 虽然OpenRTB支持这个属性，它已经与2008年被NIST撤销了
     */
    private String regionfips104;

    /**
     * 谷歌metro code; 与Nielsen DMA相似但不完全相同
     */
    private String metro;

    /**
     * 城市名，使用联合国贸易与运输位置码
     */
    private String city;

    /**
     * 邮政编码或者邮递区号
     */
    private String zip;

    /**
     * 使用UTC加或者减分钟数的方式表示的本地时间
     */
    private Integer utcoffset;
}

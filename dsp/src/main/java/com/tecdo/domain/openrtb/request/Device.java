package com.tecdo.domain.openrtb.request;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.ConnectionTypeEnum;
import com.tecdo.enums.openrtb.DeviceTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 提供用户使用的设备的详细信息。
 * 设备信息包括硬件，平台以及附加信息。设备可以是一部移动手机，桌面电脑，机顶盒或者其他数码设备。
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
public class Device extends Extension {

    /**
     * 浏览器 User-Agent 字符串（推荐）
     */
    private String ua;

    /**
     * 用用户当前位置表示设备位置（推荐）
     */
    private Geo geo;

    /**
     * 浏览器在HTTP头中设置的标准的"Do Not Track"标识（推荐）
     * 0-不限制追踪； 1-限制（不允许）追踪
     */
    private Integer dnt;

    /**
     * “限制广告跟踪”信号商业认可(例如，iOS, Android)
     * 0-跟踪是不受限制的； 1-跟踪必须根据商业指南进行限制
     */
    private Integer lmt;

    /**
     * 最接近设备的IPv4地址（推荐）
     */
    private String ip;

    /**
     * 最接近设备的IPV6地址（推荐）
     */
    private String ipv6;

    /**
     * 设备类型，参考被5.17
     * @see DeviceTypeEnum
     */
    private Integer devicetype = 0;

    /**
     * 设备制造商，例如 “Apple”
     */
    private String make;

    /**
     * 设备型号，例如 “iphone”
     */
    private String model;

    /**
     * 设备操作系统，例如 “ios"
     */
    private String os;

    /**
     * 设备操作系统版本号，例如 “3.1.2”
     */
    private String osv;

    /**
     * 设备硬件版本，例如 “5S”
     */
    private String hwv;

    /**
     * 屏幕的物理高度，以像素为单位
     */
    private Integer h;

    /**
     * 屏幕的物理宽度，以像素为单位
     */
    private Integer w;

    /**
     * 以像素每英寸表示的屏幕尺寸
     */
    private Integer ppi;

    /**
     * 设备物理像素与设备无关像素的比率
     */
    private Float pxratio;

    /**
     * 支持javascript
     * 0-不支持； 1-支持
     */
    private Integer js;

    /**
     * 地理位置API是否可用于横幅中运行的JavaScript代码
     * 0-禁止； 1-允许。
     */
    private Integer geofetch;

    /**
     * 浏览器支持的Flash版本
     */
    private String flashver;

    /**
     * 浏览器语言，使用 ISO-639-1-alpha-2
     */
    private String language;

    /**
     * ISP的附带信息（如版本号）。“WIFI"通常在移动设备中表示高带宽。（例如,video freendly vs. cellular).
     */
    private String carrier;

    /**
     * 移动运营商为串联的MCC-MNC代码(例如，“310-005”表示美国的Verizon Wireless CDMA)。
     * 参考 https://en.wikipedia.org/wiki/Mobile_country_code 更多的例子。注意，MCC和MNC部分之间的破折号是消除解析歧义所必需的。
     */
    private String mccmnc;

    /**
     * 网络连接类型，参考表5.18
     * @see ConnectionTypeEnum
     */
    private Integer connectiontype = 0;

    /**
     * 广告识别码（ID for Advertisers）：即广告目标设备唯一标识符。
     * 该标识符通常由移动操作系统生成，用于跟踪广告效果并将它们针对特定用户进行定位。
     */
    private String ifa;

    /**
     * 硬件设备ID(例如 IMEI),使用SHA1哈希算法
     */
    private String didsha1;

    /**
     * 硬件设备ID(例如 IMEI),使用md5哈希算法
     */
    private String didmd5;

    /**
     * 设备平台ID(例如 Android ID),使用SHA1哈希算法
     */
    private String dpidsha1;

    /**
     * 设备平台ID(例如 Android ID),使用md5哈希算法
     */
    private String dpidmd5;

    /**
     * 设备mac地址,使用SHA1哈希算法
     */
    private String macsha1;

    /**
     * 设备mac地址,使用md5哈希算法
     */
    private String macmd5;
}

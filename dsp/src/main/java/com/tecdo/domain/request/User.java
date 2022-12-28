package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 描述了解或者持有设备的用户的信息（也就是广告的受众）
 * 用户id是一个exchange artifact, 可能随着屏幕旋转或者其他的隐私策略改变。
 * 尽管如此，用户id必须在足够长的一段时间内保持不变，以为目标用户定向和用户访问频率限制提供合理的服务。
 *
 * Created by Zeki on 2022/12/24
 **/
@Setter
@Getter
public class User extends Extension {

    /**
     * 交易特定的用户标识（推荐 id 和 buyeruid 中至少提供一个）
     */
    private String id;

    /**
     * 买方为用户指定的ID，由 ADX 为买方映射（推荐 id 和 buyeruid 中至少提供一个）
     */
    private String buyeruid;

    /**
     * 生日年份，使用4位数字表示
     */
    private Integer yob;

    /**
     * 性别：M-男性； F-女性； O-其他类型； 不填充-未知
     */
    private String gender;

    /**
     * 逗号分隔的关键字，兴趣或者意向列表
     */
    private String keywords;

    /**
     * 可选特性，用于传递给竞拍者信息，在 ADX 的 cookie 中设置。
     * 字符串必须使用 base85 编码的 cookie，可以是任意格式。JSON加密的时候必须包括转义的引号。
     */
    private String customdata;

    /**
     * 用户家的位置信息。不必是用户的当前位置
     */
    private Geo geo;

    /**
     * 附加的用户信息， 每个 Data 对象表示一个不同的数据源
     */
    private List<com.tecdo.domain.request.Data> data;
}

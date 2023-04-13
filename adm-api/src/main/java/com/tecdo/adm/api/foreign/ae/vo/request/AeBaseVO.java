package com.tecdo.adm.api.foreign.ae.vo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 设计文档请参考：http://wiki.tec-do.com/pages/viewpage.action?pageId=154501647
 *
 * Created by Zeki on 2023/4/3
 */
@Setter
@Getter
public class AeBaseVO implements Serializable {

    @NotBlank
    private String channel;
    private Long timestamp;
    private String sign;
}

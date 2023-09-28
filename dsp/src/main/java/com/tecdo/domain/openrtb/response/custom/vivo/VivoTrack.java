package com.tecdo.domain.openrtb.response.custom.vivo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * VIVO自定义协议
 * 监测url
 *
 * Created by Zeki on 2023/9/14
 */
@Builder
@Setter
@Getter
public class VivoTrack implements Serializable {

    /**
     * 必填
     * 埋点事件类型：1-曝光；2-点击
     */
    private Integer eventType;

    /**
     * 必填
     * 埋点监测地址
     */
    private String url;
}

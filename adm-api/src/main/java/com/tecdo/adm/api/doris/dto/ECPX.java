package com.tecdo.adm.api.doris.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/8/1
 */
@Setter
@Getter
public class ECPX implements Serializable {

    private String country;
    private String bundle;
    private String adFormat;

    private Double eCPC;
    private Double eCPAEvent1;
    private Double eCPAEvent2;
    private Double eCPAEvent3;
    private Double eCPAEvent10;
    private Double eCPS;
}

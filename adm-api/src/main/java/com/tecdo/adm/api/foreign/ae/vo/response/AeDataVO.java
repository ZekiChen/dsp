package com.tecdo.adm.api.foreign.ae.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/4/4
 */
@Setter
@Getter
public class AeDataVO<T> implements Serializable {

    @JsonProperty("data_list")
    private List<T> dataList;

}

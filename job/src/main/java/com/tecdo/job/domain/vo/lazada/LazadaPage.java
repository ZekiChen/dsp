package com.tecdo.job.domain.vo.lazada;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/2/24
 */
@Setter
@Getter
public class LazadaPage<T> implements Serializable {

    private Integer total;
    @JsonProperty("total_page")
    private Integer totalPage;
    private Integer page;
    @JsonProperty("page_size")
    private Integer pageSize;
    private List<T> data;

}

package com.tecdo.adm.api.foreign.ae.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeki on 2023/4/3
 */
@Getter
@Setter
public class AeDailyCostVO extends AeBaseVO {

    @JsonProperty("bizdate")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date bizDate;

    @NotEmpty
    @JsonProperty("campaignIds")
    private List<String> advCampaignIds;
}

package com.tecdo.adm.api.delivery.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/3/15
 */
@Data
public class SimpleCampaignDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer campaignId;
	private String campaignName;

	private Integer adGroupId;
	private String adGroupName;
}
package com.tecdo.adm.api.doris.dto;

import lombok.Data;

import java.util.Date;

/**
 * Created by Elwin on 2023/12/25
 */
@Data
public class AffWeekReport {
    private String date;
    private Long request;
    private Long response;
    private Float bidRate;
    private Long wins;
    private Double winRate;
    private Float bidPrice;
    private Long imp;
    private Float cpm;
    private Float impRate;
    private Long click;
    private Float cpc;
    private Float ctr;
    private Float pCtrImp;
    private Float cost;
    private Integer orders;
    private Float orderCvr;
    private Float revenue;
    private Float roi;
    private Float orderCvrRt;
    private Float orderCpaRt;
    private Float revenueRt;
    private Float roiRt;
    private Float bidFloor;
    private Float pCvrClick;
}

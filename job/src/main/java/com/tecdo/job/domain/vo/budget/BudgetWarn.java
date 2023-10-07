package com.tecdo.job.domain.vo.budget;

import lombok.Data;

import java.text.DecimalFormat;

/**
 * Created by Elwin on 2023/9/26
 */
@Data
public class BudgetWarn {
    private String time;
    private int campaign_id;
    private int ad_group_id;
    private String sum;
    private String budget;
    private String diff;
    private String ad_group_name;
    private String campaign_name;

    public BudgetWarn(String time, int campaign_id, int ad_group_id, String sum, String budget,
                      String ad_group_name, String campaign_name) {
        // 取2位小数
        DecimalFormat df = new DecimalFormat("0.00");
        this.time = time;
        this.campaign_id = campaign_id;
        this.ad_group_id = ad_group_id;
        this.sum = df.format(Double.parseDouble(sum) / 1000);
        this.budget = budget;
        this.diff = df.format(Double.parseDouble(sum) / 1000 - Double.parseDouble(budget));
        this.ad_group_name = ad_group_name;
        this.campaign_name = campaign_name;
    }
}

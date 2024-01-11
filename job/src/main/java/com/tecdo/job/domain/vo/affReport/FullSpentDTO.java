package com.tecdo.job.domain.vo.affReport;

import com.tecdo.adm.api.delivery.dto.SpentDTO;
import lombok.Data;

/**
 * Created by Elwin on 2024/1/10
 */
@Data
public class FullSpentDTO {
    SpentDTO dspSpent;
    SpentDTO affRevenue;
    Double impGap;
    Double revenueGap;
}

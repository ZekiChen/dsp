package com.tecdo.adm.foreign.ae.service;

import com.tecdo.adm.api.foreign.ae.vo.request.AeDailyCostVO;
import com.tecdo.adm.api.foreign.ae.vo.response.AeDataVO;
import com.tecdo.adm.api.foreign.ae.vo.response.AeReportVO;

/**
 * Created by Zeki on 2023/4/4
 */
public interface IAeReportService {

    AeDataVO<AeReportVO> listAdvCampaignDailyReport(AeDailyCostVO vo);
}

package com.tecdo.adm.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.api.delivery.vo.BatchAdGroupUpdateVO;
import com.tecdo.adm.api.delivery.vo.BundleAdGroupUpdateVO;
import com.tecdo.adm.api.log.entity.BizLogApi;
import com.tecdo.starter.mp.entity.StatusEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeki on 2023/4/5
 */
public interface IBizLogApiService extends IService<BizLogApi> {

    void logByUpdateAdGroup(AdGroupVO beforeVO, AdGroupVO afterVO);

    void logByDeleteAdGroup(List<Integer> ids, List<StatusEntity> adStatusList);

    void logByUpdateAdGroupDirect(AdGroupVO beforeVO, AdGroupVO afterVO);

    void logByUpdateAdGroupBundle(TargetCondition before, TargetCondition after);

    void logByUpdateBatch(Map<Integer, AdGroup> beAdGroupMap, BatchAdGroupUpdateVO afterVO);

    void logByUpdateBatchCondition(String attribute, Map<Integer, TargetCondition> beConditonMap, BundleAdGroupUpdateVO afterVO);
}

package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.log.entity.BizLogApi;
import com.tecdo.adm.api.log.enums.BizTypeEnum;
import com.tecdo.adm.api.log.enums.OptTypeEnum;
import com.tecdo.adm.api.log.vo.BizLogApiVO;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.util.BeanUtil;

import java.util.Objects;

/**
 * 包装类，返回视图层所需的字段
 *
 * Created by Zeki on 2023/3/7
 */
public class BizLogApiWrapper extends EntityWrapper<BizLogApi, BizLogApiVO> {

	public static BizLogApiWrapper build() {
		return new BizLogApiWrapper();
	}

	@Override
	public BizLogApiVO entityVO(BizLogApi entity) {
		BizLogApiVO vo = Objects.requireNonNull(BeanUtil.copy(entity, BizLogApiVO.class));
		vo.setOptTypeName(OptTypeEnum.of(vo.getBizType()).getDesc());
		vo.setBizTypeName(BizTypeEnum.of(vo.getBizType()).getDesc());
		return vo;
	}

}
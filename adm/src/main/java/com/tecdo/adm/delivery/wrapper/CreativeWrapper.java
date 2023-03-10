package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.vo.CreativeVO;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.util.BeanUtil;

import java.util.Objects;

/**
 * Created by Zeki on 2023/3/7
 */
public class CreativeWrapper extends EntityWrapper<Creative, CreativeVO> {

	public static CreativeWrapper build() {
		return new CreativeWrapper();
	}

	@Override
	public CreativeVO entityVO(Creative creative) {
		return Objects.requireNonNull(BeanUtil.copy(creative, CreativeVO.class));
	}

}
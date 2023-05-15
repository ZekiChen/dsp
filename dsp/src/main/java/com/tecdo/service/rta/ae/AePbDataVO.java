package com.tecdo.service.rta.ae;

import com.tecdo.adm.api.foreign.ae.vo.request.AeBaseVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Zeki on 2023/4/6
 */
@Setter
@Getter
public class AePbDataVO extends AeBaseVO {

    private List<AePbInfoVO> data;
}

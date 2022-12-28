package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

/**
 * 描述任何适用于该请求的法律，政府或者工业管控条例。
 *
 * Created by Zeki on 2022/12/22
 **/
@Setter
@Getter
public class Regs extends Extension {

    /**
     * 标志着该请求是否遵从COPPA法案
     * 0-不遵从； 1-遵从
     */
    private Integer coppa;
}

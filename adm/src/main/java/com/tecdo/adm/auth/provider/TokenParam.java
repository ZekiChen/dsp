package com.tecdo.adm.auth.provider;

import com.tecdo.starter.tool.support.Kv;
import lombok.Data;

/**
 * Created by Zeki on 2023/3/13
 */
@Data
public class TokenParam {

	private Kv args = Kv.create();

}

package com.tecdo.starter.auth.domain;

import com.tecdo.starter.tool.support.Kv;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/3/14
 */
@Data
public class PacUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clientId;
    private Integer userId;
    private String account;
    private String realName;
    private String roleId;
    private String roleName;
    private Kv detail;

}

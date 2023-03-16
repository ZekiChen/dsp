package com.tecdo.starter.secure.provider;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/3/14
 */
public interface IClientDetails extends Serializable {

	String getClientId();
	String getClientSecret();

	Integer getAccessTokenValidity();
	Integer getRefreshTokenValidity();

}

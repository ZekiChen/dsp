package com.tecdo.starter.secure.provider;

/**
 * Created by Zeki on 2023/3/14
 */
public interface IClientDetailsService {

	IClientDetails loadClientByClientId(String clientId);

}

package com.tecdo.starter.secure.provider;

import com.tecdo.starter.secure.constant.SecureConstant;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Zeki on 2023/3/14
 */
@AllArgsConstructor
public class ClientDetailsServiceImpl implements IClientDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public IClientDetails loadClientByClientId(String clientId) {
        try {
            return jdbcTemplate.queryForObject(SecureConstant.DEFAULT_SELECT_STATEMENT, new String[]{clientId},
                    new BeanPropertyRowMapper<>(ClientDetails.class));
        } catch (Exception ex) {
            return null;
        }
    }

}

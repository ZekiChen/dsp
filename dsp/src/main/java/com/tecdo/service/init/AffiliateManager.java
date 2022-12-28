package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.entity.Affiliate;
import com.tecdo.mapper.AffiliateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateManager extends ServiceImpl<AffiliateMapper, Affiliate> {

    public void init() {
        List<Affiliate> affiliates = list();
        System.out.println(affiliates);
    }
}

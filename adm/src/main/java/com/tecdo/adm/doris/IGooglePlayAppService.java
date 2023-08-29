package com.tecdo.adm.doris;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;

import java.util.List;

/**
 * Created by Zeki on 2023/8/3
 */
public interface IGooglePlayAppService extends IService<GooglePlayApp> {

    List<String> listCategory();
    List<String> listTag();

    String countByCategoriesAndTags(List<String> categoryList, List<String> tagList);

    List<String> listByCategoriesAndTags(List<String> categoryList, List<String> tagList);
}

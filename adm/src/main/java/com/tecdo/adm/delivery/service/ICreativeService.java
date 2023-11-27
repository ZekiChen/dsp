package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.vo.CreativeSpecVO;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.oss.OssTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeki on 2023/3/10
 */
public interface ICreativeService extends IService<Creative> {

    List<CreativeSpecVO> listSpecs();

    List<Integer> listIdByLikeName(String name);

    List<Integer> listIdBySize(Integer width, Integer height);

    String getBrandNameByKey(String key);

    Integer getNewExternalId();

    R batchUploadFiles(MultipartFile[] files, Map<String, String> paramMap, OssTemplate ossTemplate) throws IOException;

    R<String> updateCreative(OssTemplate ossTemplate, MultipartFile file, Integer id,
                             String name, Integer width, Integer height,
                             String catIab, String suffix, Integer duration,
                             Integer status, String brand, Integer externalId) throws IOException;
}

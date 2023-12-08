package com.tecdo.job.foreign.pixalate;

import com.tecdo.starter.oss.rule.OssRule;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2023/12/6
 */
@Component
public class PixalateOssRule implements OssRule {

    @Override
    public String fileName(String originalFilename) {
        return "pixalate/" + originalFilename;
    }
}

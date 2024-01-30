package com.tecdo.job.foreign.pixalate;

import com.tecdo.starter.oss.rule.OssRule;

/**
 * Created by Elwin on 2024/1/25
 */
public class PixalatePostBidRule implements OssRule {
    @Override
    public String fileName(String originalFilename) {
        return "odl/pixalate/postbid/" + originalFilename;
    }
}

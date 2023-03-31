package com.tecdo.starter.oss.rule;

import com.tecdo.starter.tool.util.DateUtil;
import com.tecdo.starter.tool.util.FileUtil;
import com.tecdo.starter.tool.util.StringPool;
import com.tecdo.starter.tool.util.StringUtil;
import lombok.AllArgsConstructor;

/**
 * Created by Zeki on 2023/3/13
 */
@AllArgsConstructor
public class PacOssRule implements OssRule {

    @Override
    public String fileName(String originalFilename) {
        return "upload" + StringPool.SLASH + DateUtil.today() + StringPool.SLASH + StringUtil.randomUUID()
                + StringPool.DOT + FileUtil.getFileExtension(originalFilename);
    }

}

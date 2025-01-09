package com.hmall.common.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * ClassName JsonUtil
 *
 * @author qml
 * Date 2025/1/8 下午3:50
 * Version 1.0
 **/

public class FileUtil {
    public static String readResourceFileToStr(String resourcePath) throws IOException {
        Resource resource = new ClassPathResource(resourcePath);
        String filePath = resource.getFile().getPath();
        return cn.hutool.core.io.FileUtil.readUtf8String(filePath);
    }
}
package org.a8043.simpleCode.util;

import cn.hutool.core.io.FileUtil;

import java.io.File;

public class Util {
    public static String readFile(File file) {
        String content = FileUtil.readUtf8String(file);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            content = content.replace("\r\n", "\n");
        }
        return content;
    }

    public static void writeFile(String content, File file) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            content = content.replace("\n", "\r\n");
        }
        FileUtil.writeUtf8String(content, file);
    }
}

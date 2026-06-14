package org.a8043.simpleCode.cli;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import lombok.Data;
import org.a8043.simpleCode.SimpleCode;

import java.io.File;

@Data
public class CliSettings {
    public static final CliSettings INSTANCE = new CliSettings();
    private String language = "zh_cn";

    public static boolean load() {
        File file = new File(SimpleCode.SETTINGS_DIR, "cli.json");
        if (!file.exists()) {
            return false;
        }
        BeanUtil.fillBeanWithMap(new JSONObject(FileUtil.readUtf8String(file)), INSTANCE, true);
        return true;
    }
}

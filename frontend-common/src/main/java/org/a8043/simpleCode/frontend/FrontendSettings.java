package org.a8043.simpleCode.frontend;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import lombok.Data;
import org.a8043.simpleCode.SimpleCode;

import java.io.File;

@Data
public class FrontendSettings {
    public static final FrontendSettings INSTANCE = new FrontendSettings();
    private String language = "zh_cn";
    private MailSettings mail;

    @Data
    public static class MailSettings {
        private String smtpHost;
        private int smtpPort;
        private String smtpUsername;
        private String smtpPassword;
        private String fromAddress;
        private String to;
    }

    public static boolean load() {
        File file = new File(SimpleCode.DATA_DIR, "frontend.json");
        if (!file.exists()) {
            return false;
        }
        BeanUtil.fillBeanWithMap(new JSONObject(FileUtil.readUtf8String(file)), INSTANCE, true);
        return true;
    }

    public static void save() {
        FileUtil.writeUtf8String(new JSONObject(INSTANCE).toString(), new File(SimpleCode.DATA_DIR,
            "frontend.json"));
    }
}

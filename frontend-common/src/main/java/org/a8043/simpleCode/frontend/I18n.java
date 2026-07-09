package org.a8043.simpleCode.frontend;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class I18n {
    private static final Map<String, String> map = new HashMap<>();

    public static void load() {
        String content = ResourceUtil.readUtf8Str("languages/" +
                                                  FrontendSettings.INSTANCE.getLanguage() + ".json");
        new JSONObject(content).forEach((k, v) -> map.put(k, (String) v));
    }

    public static String get(String key, String... args) {
        return MessageFormat.format(map.getOrDefault(key, key), (Object[]) args);
    }
}

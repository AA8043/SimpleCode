package org.a8043.simpleCode.util;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class ModelInfo {
    private static JSONObject cache;
    private static final String API_URL = "https://models.dev/models.json";

    boolean inputImage;
    long contextLimit;

    public static ModelInfo get(String name) {
        if (cache == null) {
            HttpResponse response = HttpUtil.createGet(API_URL).execute();
            if (!response.isOk()) {
                return null;
            }
            cache = new JSONObject(response.body());
        }

        JSONObject modelInfo = cache.entrySet().stream().filter(e -> e.getKey().split("/")[1].equals(name))
            .findFirst().map(e -> (JSONObject) e.getValue()).orElse(null);
        if (modelInfo != null) {
            return new ModelInfo(modelInfo.getJSONObject("modalities").getJSONArray("input").contains("image"),
                modelInfo.getJSONObject("limit").getLong("context"));
        } else {
            return null;
        }
    }
}

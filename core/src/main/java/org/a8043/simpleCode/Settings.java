package org.a8043.simpleCode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import lombok.Data;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.model.Provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class Settings {
    public static final Settings INSTANCE = new Settings();
    private final List<Provider> providerList = new ArrayList<>();
    private final List<Model> modelList = new ArrayList<>();
    private Model currentModel;

    public Provider getProvider(String name) {
        return providerList.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    public Model getModel(Provider provider, String name) {
        return modelList.stream().filter(m -> m.getProvider().equals(provider) &&
                                              m.getName().equals(name)).findFirst().orElse(null);
    }

    public Model getLowestLevelModel() {
        return modelList.stream().max(Comparator.comparingInt(Model::getLevel)).orElse(null);
    }

    public static boolean read() {
        if (!SimpleCode.DATA_DIR.exists()) {
            return false;
        }
        File file = new File(SimpleCode.DATA_DIR, "settings.json");
        if (!file.exists()) {
            return false;
        }
        JSONObject json = new JSONObject(FileUtil.readUtf8String(file));

        json.getJSONArray("providers").forEach(o -> {
            JSONObject json1 = (JSONObject) o;
            INSTANCE.providerList.add(new Provider(json1.getStr("name"),
                json1.getStr("baseUrl"),
                json1.getStr("key"),
                Registry.API_MAP.get(json1.getStr("api"))));
        });
        json.getJSONArray("models").forEach(o -> {
            JSONObject json1 = (JSONObject) o;
            INSTANCE.modelList.add(json1.toBean(Model.class));
        });
        JSONObject currentModelJson = json.getJSONObject("currentModel");
        INSTANCE.currentModel = INSTANCE.getModel(INSTANCE.getProvider(currentModelJson.getStr("provider")),
            currentModelJson.getStr("name"));

        return true;
    }
}

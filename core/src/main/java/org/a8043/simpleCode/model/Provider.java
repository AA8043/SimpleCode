package org.a8043.simpleCode.model;

import lombok.Value;
import org.a8043.simpleCode.api.Api;

import java.util.List;

@Value
public class Provider {
    String name;
    String baseUrl;
    String key;
    Api api;

    public List<RemoteModel> requestModels() {
        return api.getModels(this);
    }
}

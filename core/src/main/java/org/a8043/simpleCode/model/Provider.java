package org.a8043.simpleCode.model;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.api.Api;
import org.a8043.simpleCode.api.ApiException;

import java.util.List;

@Slf4j
@Value
public class Provider {
    String name;
    String baseUrl;
    String key;
    Api api;

    public List<RemoteModel> requestModels() {
        try {
            return api.getModels(this);
        } catch (ApiException e) {
            log.error(e.getContent(), e);
            return List.of();
        }
    }
}

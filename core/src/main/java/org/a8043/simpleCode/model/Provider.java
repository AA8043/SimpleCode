package org.a8043.simpleCode.model;

import lombok.Value;
import org.a8043.simpleCode.api.Api;

@Value
public class Provider {
    String name;
    String baseUrl;
    String key;
    Api api;
}

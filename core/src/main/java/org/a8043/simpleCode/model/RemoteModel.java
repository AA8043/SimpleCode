package org.a8043.simpleCode.model;

import lombok.Value;

@Value
public class RemoteModel {
    Provider provider;
    String name;

    public Model toModel(int level) {
        return new Model(provider, name, level);
    }
}

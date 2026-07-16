package org.a8043.simpleCode.model;

import lombok.Value;
import org.a8043.simpleCode.util.ModelInfo;

@Value
public class RemoteModel {
    Provider provider;
    String name;
    ModelInfo modelInfo;

    public Model toModel(int level) {
        return new Model(provider, name, modelInfo, level);
    }
}

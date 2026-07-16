package org.a8043.simpleCode.model;

import lombok.Value;
import org.a8043.simpleCode.util.ModelInfo;

@Value
public class Model {
    Provider provider;
    String name;
    ModelInfo modelInfo;
    int level;
}

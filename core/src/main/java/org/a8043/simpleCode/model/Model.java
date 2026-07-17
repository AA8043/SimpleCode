package org.a8043.simpleCode.model;

import lombok.Data;
import org.a8043.simpleCode.util.ModelInfo;

@Data
public class Model {
    private final Provider provider;
    private final String name;
    private final ModelInfo modelInfo;
    private final int level;
    private boolean unavailable;
}

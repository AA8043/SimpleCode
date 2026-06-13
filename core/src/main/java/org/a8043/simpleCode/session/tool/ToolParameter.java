package org.a8043.simpleCode.session.tool;

import lombok.Getter;
import org.a8043.simpleCode.Settings;

@Getter
public class ToolParameter {
    private final String name;
    private final Type type;
    private final String description;
    private final boolean isRequired;

    public ToolParameter(String name, Type type, boolean isRequired) {
        this.name = name;
        this.type = type;
        description = Settings.PROMPT_JSON.getByPath("tool.parameters." + name, String.class);
        this.isRequired = isRequired;
    }

    public enum Type {
        STRING, NUMBER, BOOLEAN
    }
}

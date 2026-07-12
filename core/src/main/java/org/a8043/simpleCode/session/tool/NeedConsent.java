package org.a8043.simpleCode.session.tool;

import lombok.Value;

import java.util.List;

@Value
public class NeedConsent {
    boolean isNeed;
    List<String> excludedParameterList;

    public static NeedConsent unneed() {
        return new NeedConsent(false, List.of());
    }
}

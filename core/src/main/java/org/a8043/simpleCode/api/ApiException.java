package org.a8043.simpleCode.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiException extends Exception {
    private final int status;
    private final String content;
}

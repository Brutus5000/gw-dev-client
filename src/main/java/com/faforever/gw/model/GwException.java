package com.faforever.gw.model;

import lombok.Data;

@Data
public class GwException extends Exception {
    private final String errorCode;
    private final String errorMessage;

    public GwException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}

package com.faforever.gw.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorEvent {
    private String code;
    private String message;
}

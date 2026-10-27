package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public record ErrorResponseDTO(
    LocalDateTime timestamp,
    HttpStatus status,
    String message,
    String path) {
}

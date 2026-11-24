package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

/**
 * Error details returned by the trusted-party service.
 *
 * @param timestamp local date and time when the error response was created
 * @param status HTTP status associated with the error
 * @param message description of the error
 * @param path request path that produced the error
 */
public record ErrorResponseDTO(
    LocalDateTime timestamp,
    HttpStatus status,
    String message,
    String path) {
}

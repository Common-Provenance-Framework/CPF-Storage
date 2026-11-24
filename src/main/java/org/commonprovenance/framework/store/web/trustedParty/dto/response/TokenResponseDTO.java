package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasJwtToken;

/**
 * Trusted-party service response containing an issued JSON Web Token (JWT).
 *
 * @param jwt issued JWT
 */
public record TokenResponseDTO(
    String jwt) implements
    HasJwtToken {
}

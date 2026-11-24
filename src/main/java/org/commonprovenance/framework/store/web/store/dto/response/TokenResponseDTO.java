package org.commonprovenance.framework.store.web.store.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasJwtToken;

/**
 * Response containing a JSON Web Token (JWT) issued by the storage service.
 *
 * @param jwt the issued JWT
 */
public record TokenResponseDTO(
    String jwt) implements
    HasJwtToken {
}

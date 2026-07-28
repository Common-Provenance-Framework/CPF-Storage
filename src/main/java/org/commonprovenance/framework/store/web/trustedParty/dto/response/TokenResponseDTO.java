package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasJwtToken;

public record TokenResponseDTO(
    String jwt) implements
    HasJwtToken {
}

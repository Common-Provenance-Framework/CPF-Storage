package org.commonprovenance.framework.store.web.store.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasJwtToken;

public record TokenResponseDTO(
    String jwt) implements
    HasJwtToken {
}

package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasGraph;

public record DocumentResponseDTO(
    String graph) implements
    HasGraph {
}

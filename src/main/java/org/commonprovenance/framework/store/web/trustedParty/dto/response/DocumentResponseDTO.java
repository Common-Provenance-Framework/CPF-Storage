package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasGraph;

/**
 * Trusted-party service response containing serialized graph content.
 *
 * @param graph serialized graph content
 */
public record DocumentResponseDTO(
    String graph) implements
    HasGraph {
}

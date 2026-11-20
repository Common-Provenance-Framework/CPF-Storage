package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import org.commonprovenance.framework.store.common.dtos.HasGraph;
import org.commonprovenance.framework.store.common.dtos.HasOrganizationId;
import org.commonprovenance.framework.store.common.dtos.HasSignature;
import org.commonprovenance.framework.store.common.dtos.Validatable;

/**
 * Request data for verifying a provenance graph's signature with the trusted-party service.
 *
 * @param organizationId identifier of the organization associated with the graph
 * @param graph serialized graph content
 * @param signature signature to verify
 */
public record VerifySignatureFormDTO(
    String organizationId,
    String graph,
    String signature) implements
    HasOrganizationId,
    HasGraph,
    HasSignature,
    Validatable {
}

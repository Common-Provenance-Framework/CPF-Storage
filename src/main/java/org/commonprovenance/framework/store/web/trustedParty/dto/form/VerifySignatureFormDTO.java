package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import org.commonprovenance.framework.store.common.dtos.HasGraph;
import org.commonprovenance.framework.store.common.dtos.HasOrganizationId;
import org.commonprovenance.framework.store.common.dtos.HasSignature;
import org.commonprovenance.framework.store.common.dtos.Validatable;

public record VerifySignatureFormDTO(
    String organizationId,
    String graph,
    String signature) implements
    HasOrganizationId,
    HasGraph,
    HasSignature,
    Validatable {
}

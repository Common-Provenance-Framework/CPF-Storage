package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import org.commonprovenance.framework.store.common.dtos.HasCreatedOn;
import org.commonprovenance.framework.store.common.dtos.HasGraph;
import org.commonprovenance.framework.store.common.dtos.HasGraphFormat;
import org.commonprovenance.framework.store.common.dtos.HasGraphType;
import org.commonprovenance.framework.store.common.dtos.HasOrganizationId;
import org.commonprovenance.framework.store.common.dtos.HasSignature;
import org.commonprovenance.framework.store.common.dtos.Validatable;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.GraphType;

public record IssueTokenFormDTO(
    String organizationId,
    String graph,
    GraphFormat graphFormat,
    String signature,
    GraphType graphType,
    String createdOn) implements
    HasOrganizationId,
    HasGraph,
    HasGraphFormat,
    HasSignature,
    HasGraphType,
    HasCreatedOn,
    Validatable {
}

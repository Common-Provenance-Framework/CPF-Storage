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

/**
 * Request data for issuing a token for a provenance graph through the trusted-party service.
 *
 * @param organizationId identifier of the organization associated with the graph
 * @param graph          serialized graph content
 * @param graphFormat    {@link GraphFormat} used to serialize the graph
 * @param signature      signature associated with the graph; may be {@code null} when the {@link GraphType} does not require one
 * @param graphType      {@link GraphType} for which the token is requested
 * @param createdOn      timestamp at which the request data was created in UTC, represented as an ISO-8601 string
 */
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

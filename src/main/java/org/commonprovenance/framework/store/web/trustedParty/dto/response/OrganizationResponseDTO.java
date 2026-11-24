package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasId;

/**
 * Trusted-party service response containing an organization's identifier and client certificate.
 *
 * @param id organization identifier
 * @param clientCertificate organization's client certificate
 */
public record OrganizationResponseDTO(
    String id,
    String clientCertificate) implements
    HasId,
    HasClientCertificate {
}

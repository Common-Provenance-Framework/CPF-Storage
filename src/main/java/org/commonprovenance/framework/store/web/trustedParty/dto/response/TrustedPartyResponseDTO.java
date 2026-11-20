package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasId;

/**
 * Trusted-party service response containing the trusted party's identifier and client certificate.
 *
 * @param id trusted-party identifier
 * @param clientCertificate trusted party's client certificate
 */
public record TrustedPartyResponseDTO(
    String id,
    String clientCertificate) implements
    HasId,
    HasClientCertificate {
}

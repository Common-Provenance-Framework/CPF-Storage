package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasId;

public record CertificateResponseDTO(
    String id,
    String clientCertificate) implements
    HasId,
    HasClientCertificate {
}

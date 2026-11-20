package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dtos.HasId;
import org.commonprovenance.framework.store.common.dtos.Validatable;

/**
 * Request data for registering an organization with the trusted-party service.
 *
 * @param id organization identifier
 * @param clientCertificate organization's client certificate
 * @param intermediateCertificates intermediate certificates in the certificate chain
 */
public record RegisterOrganizationFormDTO(
    String id,
    String clientCertificate,
    List<String> intermediateCertificates) implements
    HasId,
    HasClientCertificate,
    HasIntermediateCertificates,
    Validatable {
}

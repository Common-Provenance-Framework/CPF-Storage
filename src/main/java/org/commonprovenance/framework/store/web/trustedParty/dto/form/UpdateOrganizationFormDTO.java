package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasId;
import org.commonprovenance.framework.store.common.dtos.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dtos.Validatable;

/**
 * Request data for updating an organization's certificates with the trusted-party service.
 *
 * @param id organization identifier
 * @param clientCertificate updated client certificate
 * @param intermediateCertificates updated intermediate certificates in the certificate chain
 */
public record UpdateOrganizationFormDTO(
    String id,
    String clientCertificate,
    List<String> intermediateCertificates) implements
    HasId,
    HasClientCertificate,
    HasIntermediateCertificates,
    Validatable {
}

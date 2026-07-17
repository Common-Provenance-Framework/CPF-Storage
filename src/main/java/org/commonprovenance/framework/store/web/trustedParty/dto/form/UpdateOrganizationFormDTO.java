package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dtos.Validatable;

public record UpdateOrganizationFormDTO(
    String clientCertificate,
    List<String> intermediateCertificates) implements
    HasClientCertificate,
    HasIntermediateCertificates,
    Validatable {
}

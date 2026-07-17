package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dtos.HasId;
import org.commonprovenance.framework.store.common.dtos.Validatable;

public record RegisterOrganizationFormDTO(
    String id,
    String clientCertificate,
    List<String> intermediateCertificates

) implements
    HasId,
    HasClientCertificate,
    HasIntermediateCertificates,
    Validatable {
}

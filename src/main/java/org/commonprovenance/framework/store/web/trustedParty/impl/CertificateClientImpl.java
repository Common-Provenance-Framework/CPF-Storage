package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.CertificateClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.DTOFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.CertificateTPResponseDTO;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class CertificateClientImpl implements CertificateClient {
  private final Client client;

  public CertificateClientImpl(
      Client client) {
    this.client = client;
  }

  private String getUri(String id) {
    return "/organizations/" + id + "/certs";
  }

  @Override
  public @NotNull Function<String, Mono<Organization>> getOrganizationCertificate(Optional<String> trustedPartyUrl) {
    return (@NotNull String organizationId) -> MONO
        .<String>makeSureNotNullWithMessage("Organization id can not be null!").apply(organizationId)
        .flatMap((String id) -> {
          return trustedPartyUrl
              .map(this.client::buildWebClient)
              .map(this.client.sendCustomGetOneRequest(getUri(id), CertificateTPResponseDTO.class))
              .orElse(this.client.sendGetOneRequest(getUri(id), CertificateTPResponseDTO.class));
        })
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Function<Organization, Mono<Organization>> updateOrganizationCertificate(
      Optional<String> trustedPartyUrl) {
    return (@NotNull Organization organization) -> DTOFactory.toUpdateForm(organization)
        .flatMap(trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomPutRequest(getUri(organization.getName()), Void.class))
            .orElse(this.client.sendPutRequest(getUri(organization.getName()), Void.class)))
        .thenReturn(organization);
  }

}

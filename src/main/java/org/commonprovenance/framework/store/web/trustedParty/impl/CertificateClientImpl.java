package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.List;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.CertificateClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationTPFormDTO;
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

  private Mono<CertificateTPResponseDTO> getOneReq(String id) {
    return client.sendGetOneRequest(
        "/organizations/" + id + "/certs",
        CertificateTPResponseDTO.class);
  }

  private Function<UpdateOrganizationTPFormDTO, Mono<Void>> putReq(String id) {
    return (UpdateOrganizationTPFormDTO body) -> client.sendPutRequest(
        "/organizations" + id + "/certs",
        Void.class)
        .apply(body);
  }

  @Override
  public @NotNull Mono<Organization> getOrganizationCertificate(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Organization id can not be null!").apply(id)
        .flatMap(this::getOneReq)
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Boolean> updateOrganizationCertificate(
      @NotNull String id,
      @NotNull String clientCertificate,
      @NotNull List<String> intermediateCertificates) {
    return DTOFactory.toForm(clientCertificate, intermediateCertificates)
        .flatMap(this.putReq(id))
        .thenReturn(true);
  }

}

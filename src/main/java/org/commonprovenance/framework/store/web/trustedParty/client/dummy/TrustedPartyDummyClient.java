package org.commonprovenance.framework.store.web.trustedParty.client.dummy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.commonprovenance.framework.store.web.trustedParty.client.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("dummy")
@Component
public class TrustedPartyDummyClient implements TrustedPartyClient {
  private static Map<String, OrganizationResponseDTO> organizations;

  public TrustedPartyDummyClient() {
    organizations = new HashMap<>();
  }

  private String extractIdFromUri(String uri) {
    return Arrays.stream(uri.split("/"))
        .filter(s -> !s.isEmpty())
        .reduce((first, second) -> second)
        .orElse("");
  }

  @Override
  public <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType) {
    if (responseType.equals(OrganizationResponseDTO.class)) {
      String id = extractIdFromUri(uri);

      OrganizationResponseDTO dto = organizations.get(id);
      return Mono.justOrEmpty(dto).map(responseType::cast);
    }

    return Mono.empty();
  }

  @Override
  public <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType) {
    if (responseType.equals(OrganizationResponseDTO.class)) {
      return Flux.fromIterable(organizations.values())
          .map(responseType::cast);
    }

    return Flux.empty();
  }

  @Override
  public <T, B> Mono<T> sendPostRequest(String uri, B body, Class<T> responseType) {
    if (responseType.equals(OrganizationResponseDTO.class)
        && body instanceof OrganizationFormDTO orgForm) {
      String id = UUID.randomUUID().toString();
      OrganizationResponseDTO dto = new OrganizationResponseDTO(id, orgForm.getName());
      organizations.put(id, dto);
      return Mono.just(dto).map(responseType::cast);
    }

    return Mono.empty();
  }

  @Override
  public <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType) {
    if (responseType.equals(OrganizationResponseDTO.class)) {
      String id = extractIdFromUri(uri);

      OrganizationResponseDTO dto = organizations.remove(id);
      return Mono.justOrEmpty(dto).map(responseType::cast);
    }

    return Mono.empty();
  }
}

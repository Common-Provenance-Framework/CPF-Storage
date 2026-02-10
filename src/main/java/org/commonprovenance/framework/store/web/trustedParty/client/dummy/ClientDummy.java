package org.commonprovenance.framework.store.web.trustedParty.client.dummy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationTPFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyTPResponseDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("dummy")
@Component
public class ClientDummy implements Client {
  private static Map<String, OrganizationTPResponseDTO> organizations;
  private static Map<String, TokenTPResponseDTO> tokens;
  private static TrustedPartyTPResponseDTO INFO = new TrustedPartyTPResponseDTO(
      "Trusted_Party",
      "-----BEGIN CERTIFICATE-----\nMIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw\nbTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug\nRGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp\nY2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd\nMQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE\nZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw\nEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9\n+KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD\nVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji\nXokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ\nMAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1\nFRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=\n-----END CERTIFICATE-----\n");

  public ClientDummy() {
    organizations = new HashMap<>();
    tokens = new HashMap<>();
  }

  private Optional<String> extractIdFromUri(String uri) {
    return Arrays.stream(uri.split("/"))
        .filter(s -> !s.isEmpty())
        .reduce((first, second) -> second);
  }

  @Override
  public String getUrl() {
    // ignore custom TP for now
    // TODO: index etities by TP id
    return "http://trusted-party.org/api/v1";
  }

  @Override
  public <T> Function<WebClient, Mono<T>> sendCustomGetOneRequest(String uri, Class<T> responseType) {
    // ignore custom TP for now
    // TODO: index etities by TP id
    return (WebClient _) -> sendGetOneRequest(uri, responseType);
  }

  @Override
  public <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType) {
    return Mono.justOrEmpty(extractIdFromUri(uri))
        .flatMap((String id) -> {
          if (responseType.equals(OrganizationTPResponseDTO.class)) {
            return Mono.justOrEmpty(organizations.get(id));
          } else if (responseType.equals(TokenTPResponseDTO.class)) {
            return Mono.justOrEmpty(tokens.get(id));
          } else if (responseType.equals(TrustedPartyTPResponseDTO.class)) {
            return Mono.just(INFO);
          } else {
            return Mono.empty();
          }
        })
        .map(responseType::cast);
  }

  @Override
  public <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType) {
    if (responseType.equals(OrganizationTPResponseDTO.class)) {
      return Flux.fromIterable(organizations.values())
          .map(responseType::cast);
    } else if (responseType.equals(TokenTPResponseDTO.class)) {
      return Flux.fromIterable(tokens.values())
          .map(responseType::cast);
    } else {
      return Flux.empty();
    }
  }

  @Override
  public <T, B> Function<B, Mono<T>> sendPostRequest(String uri, Class<T> responseType) {
    if (responseType.equals(OrganizationTPResponseDTO.class)) {
      return (B body) -> {
        if (body instanceof RegisterOrganizationTPFormDTO orgForm) {
          OrganizationTPResponseDTO dto = new OrganizationTPResponseDTO(
              orgForm.getOrganizationId(),
              orgForm.getClientCertificate());
          organizations.put(dto.getId(), dto);
          return Mono.just(dto).map(responseType::cast);
        }
        return Mono.empty();
      };
    }
    return _ -> Mono.empty();
  }

  @Override
  public <T, B> Function<WebClient, Function<B, Mono<T>>> sendCustomPostRequest(String uri, Class<T> responseType) {
    // ignore custom TP for now
    // TODO: index etities by TP id
    return (WebClient _) -> sendPostRequest(uri, responseType);
  }

  @Override
  public <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType) {
    return Mono.justOrEmpty(extractIdFromUri(uri))
        .flatMap((String id) -> {
          if (responseType.equals(OrganizationTPResponseDTO.class)) {
            return Mono.justOrEmpty(organizations.remove(id));
          } else if (responseType.equals(TokenTPResponseDTO.class)) {
            return Mono.justOrEmpty(tokens.remove(id));
          } else {
            return Mono.empty();
          }
        })
        .map(responseType::cast);
  }
}

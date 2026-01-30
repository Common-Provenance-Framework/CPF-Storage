package org.commonprovenance.framework.store.web.trustedParty.dummy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationsClient;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.mapper.DomainMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("trustedparty & dummy")
@Component
public class OrganizationDummyClient implements OrganizationsClient {
  private static Map<UUID, OrganizationResponseDTO> organizations;

  public OrganizationDummyClient() {
    organizations = new HashMap<>();
  }

  @Override
  public @NotNull Mono<Organization> create(@NotNull String name) {
    return Mono.just(name)
        .map(OrganizationFormDTO::factory)
        .map(org -> {
          UUID id = UUID.randomUUID();
          OrganizationResponseDTO dto = new OrganizationResponseDTO(id.toString(), name);
          organizations.put(id, dto);
          return dto;
        })
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  public @NotNull Flux<Organization> getAll() {
    return Flux.fromIterable(organizations.values())
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  public @NotNull Mono<Organization> getById(@NotNull UUID id) {
    return Mono.just(organizations.get(id))
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  public @NotNull Mono<Void> deleteById(@NotNull UUID id) {
    return Mono.just(organizations.remove(id))
        .then();
  }
}

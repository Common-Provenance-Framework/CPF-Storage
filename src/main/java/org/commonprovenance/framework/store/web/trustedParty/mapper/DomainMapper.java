package org.commonprovenance.framework.store.web.trustedParty.mapper;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;

import reactor.core.publisher.Mono;

public class DomainMapper {
  public static Mono<Organization> toDomain(OrganizationResponseDTO dto) {
    try {
      UUID id = UUID.fromString(dto.getId());
      return Mono.just(new Organization(id, dto.getName()));
    } catch (IllegalArgumentException illegalArgumentException) {
      return Mono.error(new InternalApplicationException(
          "Id '" + dto.getId() + "' is not valid UUID string.",
          illegalArgumentException));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }
}

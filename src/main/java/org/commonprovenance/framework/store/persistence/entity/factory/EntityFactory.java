package org.commonprovenance.framework.store.persistence.entity.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;

import reactor.core.publisher.Mono;

public class EntityFactory {
  private static DocumentEntity fromModel(Document model) {
    return new DocumentEntity(
        model.getId().toString(),
        model.getGraph(),
        model.getFormat().toString());
  }

  private static OrganizationEntity fromModel(Organization organization) {
    return new OrganizationEntity(
        organization.getId().map(UUID::toString).orElse(UUID.randomUUID().toString()),
        organization.getName(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  // ---

  public static Mono<DocumentEntity> toEntity(Document document) {
    return MONO.makeSureNotNull(document)
        .map(EntityFactory::fromModel);
  }

  public static Mono<OrganizationEntity> toEntity(Organization organization) {
    return MONO.makeSureNotNull(organization)
        .map(EntityFactory::fromModel);
  }
}

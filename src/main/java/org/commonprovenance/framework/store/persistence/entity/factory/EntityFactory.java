package org.commonprovenance.framework.store.persistence.entity.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.UUID;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;

import reactor.core.publisher.Mono;

public class EntityFactory {
  private static Mono<DocumentEntity> fromModel(Document model) {
    return Mono.just(model.getFormat())
        .flatMap(MONO.makeSureNotNullWithMessage("Doucument format can not be null!"))
        .flatMap(MONO.makeSure(Optional::isPresent, "Document format is missing!"))
        .map(Optional::get)
        .map((Format format) -> new DocumentEntity(
            model.getId().orElse(UUID.randomUUID()).toString(),
            model.getGraph(),
            format.toString(),
            model.getSignature()));
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
        .flatMap(EntityFactory::fromModel);
  }

  public static Mono<OrganizationEntity> toEntity(Organization organization) {
    return MONO.makeSureNotNull(organization)
        .map(EntityFactory::fromModel);
  }
}

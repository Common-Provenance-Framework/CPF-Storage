package org.commonprovenance.framework.store.persistence.entity.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;
import org.commonprovenance.framework.store.persistence.entity.TokenEntity;
import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;

import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

public class EntityFactory {
  private static Mono<DocumentEntity> fromModel(Document model) {
    return Mono.just(model.getFormat())
        .flatMap(MONO.makeSureNotNullWithMessage("Doucument format can not be null!"))
        .flatMap(MONO.makeSure(Optional::isPresent, "Document format is missing!"))
        .map(Optional::get)
        .map((Format format) -> new DocumentEntity(
            model.getId().orElse(UUID.randomUUID()).toString(),
            model.getGraph(),
            format.toString()));
  }

  private static OrganizationEntity fromModel(Organization model) {
    return new OrganizationEntity(
        model.getId().map(UUID::toString).orElse(UUID.randomUUID().toString()),
        model.getName(),
        model.getClientCertificate(),
        model.getIntermediateCertificates());
  }

  private static TrustedPartyEntity fromModel(TrustedParty model) {
    return new TrustedPartyEntity(
        model.getId().map(UUID::toString).orElse(UUID.randomUUID().toString()),
        model.getName(),
        model.getCertificate(),
        model.getUrl().orElse(null),
        model.getIsChecked(),
        model.getIsValid(),
        model.getIsDefault());
  }

  private static Mono<TokenEntity> fromModel(Token model) {
    try {
      return Mono.just(new TokenEntity(
          model.getId().map(UUID::toString).orElse(UUID.randomUUID().toString()),
          model.getHash(),
          model.getSignature(),
          model.getAdditionalData().getOriginatorName(),
          new ObjectMapper().writeValueAsString(model.getAdditionalData()),
          model.getAdditionalData().getDocumentTimestamp(),
          model.getCreatedOn()));

    } catch (Exception e) {
      return Mono.error(new InternalApplicationException("Can not create TokenEntity from Token"));
    }
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

  public static Mono<TrustedPartyEntity> toEntity(TrustedParty trustedParty) {
    return MONO.makeSureNotNull(trustedParty)
        .map(EntityFactory::fromModel);
  }

  public static Mono<TokenEntity> toEntity(Token token) {
    return MONO.makeSureNotNull(token)
        .flatMap(EntityFactory::fromModel);
  }
}

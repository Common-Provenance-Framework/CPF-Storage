package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;

import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

public class NodeFactory {
  private static Mono<DocumentNode> fromModel(Document model) {
    return Mono.just(model.getFormat())
        .flatMap(MONO.makeSureNotNullWithMessage("Doucument format can not be null!"))
        .flatMap(MONO.makeSure(Optional::isPresent, "Document format is missing!"))
        .map(Optional::get)
        .map((Format format) -> new DocumentNode(
            model.getId().orElse(UUID.randomUUID()).toString(),
            model.getGraph(),
            format.toString()));
  }

  private static OrganizationNode fromModel(Organization model) {
    return new OrganizationNode(
        model.getId().map(UUID::toString).orElse(UUID.randomUUID().toString()),
        model.getName(),
        model.getClientCertificate(),
        model.getIntermediateCertificates());
  }

  private static TrustedPartyNode fromModel(TrustedParty model) {
    return new TrustedPartyNode(
        model.getId().map(UUID::toString).orElse(UUID.randomUUID().toString()),
        model.getName(),
        model.getCertificate(),
        model.getUrl().orElse(null),
        model.getIsChecked(),
        model.getIsValid(),
        model.getIsDefault());
  }

  private static Mono<TokenNode> fromModel(Token model) {
    try {
      return Mono.just(new TokenNode(
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

  public static Mono<DocumentNode> toEntity(Document document) {
    return MONO.makeSureNotNull(document)
        .flatMap(NodeFactory::fromModel);
  }

  public static Mono<OrganizationNode> toEntity(Organization organization) {
    return MONO.makeSureNotNull(organization)
        .map(NodeFactory::fromModel);
  }

  public static Mono<TrustedPartyNode> toEntity(TrustedParty trustedParty) {
    return MONO.makeSureNotNull(trustedParty)
        .map(NodeFactory::fromModel);
  }

  public static Mono<TokenNode> toEntity(Token token) {
    return MONO.makeSureNotNull(token)
        .flatMap(NodeFactory::fromModel)
        .flatMap(entity -> NodeFactory.toEntity(token.getTrustedParty()).map(entity::withTrustedParty))
        .flatMap(entity -> NodeFactory.toEntity(token.getDocument()).map(entity::withDocument));
  }
}

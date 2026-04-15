package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.common.dto.HasOptionalFormat;
import org.commonprovenance.framework.store.common.dto.HasOptionalIdentifier;
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

public class NodeFactory {

  private static <T extends HasOptionalIdentifier> Mono<String> getIdentifier(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasOptionalIdentifier::getIdentifier)
        .flatMap(Mono::justOrEmpty)
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new InternalApplicationException("Document do not have valid identifier!"))));
  }

  private static <T extends HasOptionalFormat> Mono<String> getFormat(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasOptionalFormat::getFormat)
        .flatMap(Mono::justOrEmpty)
        .map(Format::toString)
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new InternalApplicationException("Document do not have valid format"))));
  }

  private static DocumentNode fromModel(Document model) {
    return new DocumentNode(model.getGraph());
  }

  private static OrganizationNode fromModel(Organization model) {
    OrganizationNode node = new OrganizationNode(
        model.getIdentifier(),
        model.getClientCertificate(),
        model.getIntermediateCertificates());

    return model.getId().map(node::withId)
        .orElse(node);
  }

  private static TrustedPartyNode fromModel(TrustedParty model) {
    TrustedPartyNode node = new TrustedPartyNode(
        model.getName(),
        model.getCertificate(),
        model.getUrl().orElse(null),
        model.getIsChecked(),
        model.getIsValid(),
        model.getIsDefault());

    return model.getId().map(node::withId)
        .orElse(node);
  }

  private static TokenNode fromModel(Token model) {
    return new TokenNode(model.getJwt());
  }

  // ---

  public static Mono<DocumentNode> toEntity(Document document) {
    return MONO.makeSureNotNull(document)
        .map(NodeFactory::fromModel)
        .flatMap(node -> NodeFactory.getIdentifier(document).map(node::withIdentifier))
        .flatMap(node -> NodeFactory.getFormat(document).map(node::withFormat));
  }

  public static Mono<OrganizationNode> toEntity(Organization organization) {
    return Mono.zip(
        MONO.makeSureNotNull(organization).map(NodeFactory::fromModel),
        Mono.justOrEmpty(organization.getTrustedParty()).map(NodeFactory::fromModel))
        .map(t -> t.getT1().withTrustedParty(t.getT2()));

  }

  public static Mono<TrustedPartyNode> toEntity(TrustedParty trustedParty) {
    return MONO.makeSureNotNull(trustedParty)
        .map(NodeFactory::fromModel);
  }

  public static Mono<TokenNode> toEntity(Token token) {
    return MONO.makeSureNotNull(token)
        .map(NodeFactory::fromModel)
        .flatMap(entity -> NodeFactory.toEntity(token.getTrustedParty()).map(entity::withTrustedParty))
        .flatMap(entity -> NodeFactory.toEntity(token.getDocument()).map(entity::withDocument));
  }
}

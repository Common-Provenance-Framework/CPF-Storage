package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasDocument;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.dto.HasSignature;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationTPFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationTPFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.VerifySignatureTPFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenTPFormDTO;

import reactor.core.publisher.Mono;

public class DTOFactory {

  private static <T extends HasCreatedOn<T>> UnaryOperator<T> addCreatedOn() {
    return (T form) -> form.withCreatedOn(Instant.now().getEpochSecond());
  }

  private static UnaryOperator<IssueTokenTPFormDTO> addFormatfromDocumentModel(Document model) {
    return form -> Optional
        .ofNullable(model)
        .flatMap(Document::getFormat)
        .map(Format::toString)
        .map(String::toLowerCase)
        .map(form::withDocumentFormat)
        .orElse(form);
  }

  // private static UnaryOperator<IssueTokenTPFormDTO>
  // addOrganizationIdentifierfromDocumentModel(Document model) {
  // return form -> Optional
  // .ofNullable(model)
  // .flatMap(Document::getOrganizationIdentifier)
  // .map(form::withOrganizationIdentifier)
  // .orElse(form);
  // }

  private static <T extends HasSignature<T>> UnaryOperator<T> addSignatureFromDocumentModel(Document model) {
    return form -> Optional
        .ofNullable(model)
        .map(Document::getSignature)
        .flatMap(Optional::ofNullable)
        .map(form::withSignature)
        .orElse(form);
  }

  private static UnaryOperator<IssueTokenTPFormDTO> addTypeFromGraphType(GraphType graphType) {
    return form -> Optional
        .ofNullable(graphType)
        .map(GraphType::toString)
        .map(String::toLowerCase)
        .map(form::withGraphType)
        .orElse(form);
  }

  // ---

  public static Mono<IssueTokenTPFormDTO> toForm(Document document, GraphType graphType) {

    return Mono.justOrEmpty(MonoidComposition.compose(
        new IssueTokenTPFormDTO(),
        List.of(
            HasOrganizationId.addIdentifier(document),
            HasDocument.addGraph(document),
            addFormatfromDocumentModel(document),
            addSignatureFromDocumentModel(document),
            addTypeFromGraphType(graphType),
            addCreatedOn())))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<RegisterOrganizationTPFormDTO> toForm(Organization organization) {
    return Mono.justOrEmpty(new RegisterOrganizationTPFormDTO(
        organization.getIdentifier(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates()))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<UpdateOrganizationTPFormDTO> toUpdateForm(Organization organization) {
    return Mono.justOrEmpty(new UpdateOrganizationTPFormDTO(
        organization.getClientCertificate(),
        organization.getIntermediateCertificates()))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<VerifySignatureTPFormDTO> toForm(Organization organization, Document document) {
    return Mono.justOrEmpty(MonoidComposition.compose(
        new VerifySignatureTPFormDTO(),
        List.of(
            HasOrganizationId.addIdentifier(organization),
            HasDocument.addGraph(document),
            HasSignature.addSignature(document))))
        .flatMap(MONO::validateDTO);
  }
}

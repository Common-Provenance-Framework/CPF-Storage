package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasDocument;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
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
  // private static UnaryOperator<RegisterOrganizationTPFormDTO>
  // organizationFormFromOrganizationModel(
  // Organization model) {
  // return form -> Optional.ofNullable(model)
  // .map(organization -> new RegisterOrganizationTPFormDTO(
  // model.getName(),
  // model.getClientCertificate(),
  // model.getIntermediateCertificates()))
  // .orElse(form);
  // }

  private static <T extends HasCreatedOn<T>> UnaryOperator<T> addCreatedOn() {
    return (T form) -> form.withCreatedOn(System.currentTimeMillis());
  }

  private static <T extends HasOrganizationId<T>> UnaryOperator<T> addOrganizationId(Organization model) {
    return (T form) -> Optional.ofNullable(model)
        .map(Organization::getName)
        .flatMap(Optional::ofNullable)
        .map(form::withOrganizationId)
        .orElse(form);
  }

  private static <T extends HasDocument<T>> UnaryOperator<T> addGraph(Document model) {
    return (T form) -> Optional.ofNullable(model)
        .map(Document::getGraph)
        .flatMap(Optional::ofNullable)
        .map(form::withDocument)
        .orElse(form);
  }

  private static UnaryOperator<IssueTokenTPFormDTO> tokenFormFromGraphTypeModel(GraphType graphType) {
    return form -> Optional.ofNullable(graphType)
        .map(GraphType::toString)
        .map(form::withGraphType)
        .orElse(form);
  }

  private static UnaryOperator<IssueTokenTPFormDTO> tokenFormFromDocumentModel(Document model) {
    Function<Document, UnaryOperator<IssueTokenTPFormDTO>> addFormat = document -> form -> Optional
        .ofNullable(document)
        .flatMap(Document::getFormat)
        .map(Format::toString)
        .map(form::withDocumentFormat)
        .orElse(form);

    Function<Document, UnaryOperator<IssueTokenTPFormDTO>> addSignature = document -> form -> Optional
        .ofNullable(document)
        .map(Document::getSignature)
        .flatMap(Optional::ofNullable)
        .map(form::withSignature)
        .orElse(form);

    return MonoidComposition.composeOperators(List.of(
        addGraph(model),
        addFormat.apply(model),
        addSignature.apply(model)));
  }

  private static UnaryOperator<VerifySignatureTPFormDTO> verifySigFormFromDocumentModel(Document model) {
    Function<Document, UnaryOperator<VerifySignatureTPFormDTO>> fromTokenSig = document -> form -> Optional
        .ofNullable(document)
        .map(Document::getSignature)
        .flatMap(Optional::ofNullable)
        .map(form::withSignature)
        .orElse(form);

    return MonoidComposition.composeOperators(List.of(
        addGraph(model),
        fromTokenSig.apply(model)));
  }

  // ---

  public static Mono<IssueTokenTPFormDTO> toForm(Organization organization, Document document, GraphType graphType) {
    Function<GraphType, UnaryOperator<IssueTokenTPFormDTO>> addGrapType = type -> form -> Optional
        .ofNullable(type)
        .map(GraphType::toString)
        .map(form::withGraphType)
        .orElse(form);

    return Mono.justOrEmpty(MonoidComposition.compose(
        new IssueTokenTPFormDTO(),
        List.of(
            addOrganizationId(organization),
            tokenFormFromDocumentModel(document),
            addGrapType.apply(graphType),
            addCreatedOn())))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<RegisterOrganizationTPFormDTO> toForm(Organization model) {
    return Mono.justOrEmpty(new RegisterOrganizationTPFormDTO(
        model.getName(),
        model.getClientCertificate(),
        model.getIntermediateCertificates()))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<UpdateOrganizationTPFormDTO> toForm(
      String clientCertificate,
      List<String> intermediateCertificates) {
    return Mono.justOrEmpty(new UpdateOrganizationTPFormDTO(
        clientCertificate,
        intermediateCertificates))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<VerifySignatureTPFormDTO> toForm(Organization organization, Document document) {
    return Mono.justOrEmpty(MonoidComposition.compose(
        new VerifySignatureTPFormDTO(),
        List.of(
            addOrganizationId(organization),
            verifySigFormFromDocumentModel(document))))
        .flatMap(MONO::validateDTO);
  }
}

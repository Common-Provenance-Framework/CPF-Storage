package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationTPFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenTPFormDTO;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static UnaryOperator<RegisterOrganizationTPFormDTO> organizationFormFromOrganizationModel(
      Organization model) {
    return form -> Optional.ofNullable(model)
        .map(organization -> new RegisterOrganizationTPFormDTO(
            model.getName(),
            model.getClientCertificate(),
            model.getIntermediateCertificates()))
        .orElse(form);
  }

  private static UnaryOperator<IssueTokenTPFormDTO> tokenFormFromOrganizationModel(Organization model) {
    return form -> Optional.ofNullable(model)
        .flatMap(Organization::getId)
        .map(UUID::toString)
        .map(form::withOrganizationId)
        .orElse(form);
  }

  private static UnaryOperator<IssueTokenTPFormDTO> tokenFormFromGraphTypeModel(GraphType graphType) {
    return form -> Optional.ofNullable(graphType)
        .map(GraphType::toString)
        .map(form::withGraphType)
        .orElse(form);
  }

  private static UnaryOperator<IssueTokenTPFormDTO> tokenFormFromDocumentModel(Document model) {
    Function<Document, UnaryOperator<IssueTokenTPFormDTO>> fromDocumentGraphModel = document -> form -> Optional
        .ofNullable(model)
        .map(Document::getGraph)
        .flatMap(Optional::ofNullable)
        .map(form::withDocument)
        .orElse(form);

    Function<Document, UnaryOperator<IssueTokenTPFormDTO>> fromDocumentFormatModel = document -> form -> Optional
        .ofNullable(document)
        .flatMap(Document::getFormat)
        .map(Format::toString)
        .map(form::withDocumentFormat)
        .orElse(form);

    return MonoidComposition.composeOperators(List.of(
        fromDocumentGraphModel.apply(model),
        fromDocumentFormatModel.apply(model)));
  }

  // ---

  public static Mono<RegisterOrganizationTPFormDTO> toForm(Organization model) {
    return Mono.justOrEmpty(MonoidComposition.compose(
        new RegisterOrganizationTPFormDTO(),
        List.of(organizationFormFromOrganizationModel(model))))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<IssueTokenTPFormDTO> toForm(Organization organization, Document document, GraphType graphType) {
    return Mono.justOrEmpty(MonoidComposition.compose(
        new IssueTokenTPFormDTO(),
        List.of(
            tokenFormFromOrganizationModel(organization),
            tokenFormFromGraphTypeModel(graphType),
            tokenFormFromDocumentModel(document))))
        .flatMap(MONO::validateDTO);
  }
}

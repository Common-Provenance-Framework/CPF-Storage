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
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationTPFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.TokenTPFormDTO;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static UnaryOperator<OrganizationTPFormDTO> organizationFormFromOrganizationModel(Organization model) {
    return form -> Optional.ofNullable(model)
        .map(organization -> new OrganizationTPFormDTO(
            model.getName(),
            model.getClientCertificate(),
            model.getIntermediateCertificates()))
        .orElse(form);
  }

  private static UnaryOperator<TokenTPFormDTO> tokenFormFromOrganizationModel(Organization model) {
    return form -> Optional.ofNullable(model)
        .flatMap(Organization::getId)
        .map(UUID::toString)
        .map(form::withOrganizationId)
        .orElse(form);
  }

  private static UnaryOperator<TokenTPFormDTO> tokenFormFromGraphTypeModel(GraphType graphType) {
    return form -> Optional.ofNullable(graphType)
        .map(GraphType::toString)
        .map(form::withGraphType)
        .orElse(form);
  }

  private static UnaryOperator<TokenTPFormDTO> tokenFormFromDocumentModel(Document model) {
    Function<Document, UnaryOperator<TokenTPFormDTO>> fromDocumentGraphModel = document -> form -> Optional
        .ofNullable(model)
        .map(Document::getGraph)
        .flatMap(Optional::ofNullable)
        .map(form::withDocument)
        .orElse(form);

    Function<Document, UnaryOperator<TokenTPFormDTO>> fromDocumentFormatModel = document -> form -> Optional
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

  public static Mono<OrganizationTPFormDTO> toForm(Organization model) {
    return Mono.justOrEmpty(MonoidComposition.compose(
        new OrganizationTPFormDTO(),
        List.of(organizationFormFromOrganizationModel(model))))
        .flatMap(MONO::validateDTO);
  }

  public static Mono<TokenTPFormDTO> toForm(Organization organization, Document document, GraphType graphType) {
    return Mono.justOrEmpty(MonoidComposition.compose(
        new TokenTPFormDTO(),
        List.of(
            tokenFormFromOrganizationModel(organization),
            tokenFormFromGraphTypeModel(graphType),
            tokenFormFromDocumentModel(document))))
        .flatMap(MONO::validateDTO);
  }
}

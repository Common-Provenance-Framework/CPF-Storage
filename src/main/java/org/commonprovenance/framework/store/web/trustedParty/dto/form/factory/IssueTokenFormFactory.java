package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasDocumentGraph;
import org.commonprovenance.framework.store.common.dto.HasDocumentOptional;
import org.commonprovenance.framework.store.common.dto.HasFormatSerialized;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.dto.HasTokenFormat;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenTPFormDTO;

import io.vavr.control.Either;

public class IssueTokenFormFactory {

  private static <T extends HasIdentifier<T> & HasDocumentOptional<T>> UnaryOperator<IssueTokenTPFormDTO> mapper(T data) {
    return MonoidComposition.<IssueTokenTPFormDTO> composeOperators(
        List.of(
            HasOrganizationId.addOrganizationId(data),
            HasDocumentGraph.addDocument(data.getDocument()),
            HasFormatSerialized.addFormat(data.getDocument()),
            HasCreatedOn.setCurrentTimeSecond(),
            HasTokenFormat.setJwtFormat()));
  }

  public static Either<ApplicationException, IssueTokenTPFormDTO> build(Organization organization, String signature) {
    return Either.<ApplicationException, IssueTokenTPFormDTO> right(new IssueTokenTPFormDTO())
        .map(IssueTokenFormFactory.mapper(organization))
        .map(form -> form.withSignature(signature))
        .map(form -> form.withType(GraphType.GRAPH))
        .flatMap(EITHER::validateDTO);
  }

  public static Either<ApplicationException, IssueTokenTPFormDTO> build(Organization organization, GraphType graphType) {
    return Either.<ApplicationException, IssueTokenTPFormDTO> right(new IssueTokenTPFormDTO())
        .map(IssueTokenFormFactory.mapper(organization))
        .map(form -> form.withType(graphType))
        .flatMap(EITHER::validateDTO);
  }
}

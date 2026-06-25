package org.commonprovenance.framework.store.model.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.Monoid;
import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasGraph;
import org.commonprovenance.framework.store.common.dto.HasTokenOptional;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentTPResponseDTO;

import io.vavr.control.Either;

public class DocumentFactory {
  private static <T> UnaryOperator<Document> mapper(T data) {
    return (Document document) -> Monoid.compose(
        document,
        List.of(
            HasGraph.addGraphIfPresent(data),
            HasFormat.addFormatIfPresent(data)));
  }

  private static <T extends HasGraph<T> & HasFormat<T>> UnaryOperator<Document> mapper(T data) {
    return (Document document) -> Monoid.compose(
        document,
        List.of(
            HasGraph.addGraph(data),
            HasFormat.addFormat(data)));
  }

  public static Document build(DocumentTPResponseDTO data) {
    return mapper(data).apply(new Document())
        .withFormat(Format.JSON);
  }

  public static Document build(DocumentFormDTO form) {
    return mapper(form).apply(new Document());
  }

  public static Either<ApplicationException, Document> build(DocumentNode node) {
    return Either.<ApplicationException, Document> right(new Document())
        .map(DocumentFactory.mapper(node))
        .flatMap(EITHER::validateDTO);
  }

  public static Either<ApplicationException, Document> buildWithRelations(DocumentNode node) {
    return Either.<ApplicationException, Document> right(new Document())
        .map(DocumentFactory.mapper(node))
        .flatMap(HasTokenOptional.addToken(node))
        .flatMap(EITHER::validateDTO);
  }

}

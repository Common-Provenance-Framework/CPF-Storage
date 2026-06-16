package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.factory.DocumentFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasDocumentNodes;

import io.vavr.control.Either;

public interface HasDocumentOptional<T extends HasDocumentOptional<T>> {
  Optional<Document> getDocument();

  T withDocument(Document document);

  static <T extends HasDocumentOptional<T>, F extends HasDocumentOptional<F>> UnaryOperator<T> addDocument(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(F::getDocument)
        .map(to::withDocument)
        .orElse(to);
  }

  static <T extends HasDocumentOptional<T>, F extends HasDocument<F>> UnaryOperator<T> addDocument(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getDocument)
        .map(to::withDocument)
        .orElse(to);
  }

  static <T extends HasDocumentOptional<T>, F extends HasDocumentNodes> Function<T, Either<ApplicationException, T>> addDocument(F from) {
    return (T to) -> Either.<ApplicationException, F> right(from)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("From Object can not be null!")))
        .map(F::getDocuments)
        .flatMap(EITHER.<List<DocumentNode>> makeSure(
            documents -> documents.size() <= 1,
            documents -> new InvalidValueException("Max one Document expected, got " + documents.size() + "!")))
        .flatMap(documents -> documents.size() == 0
            ? Either.<ApplicationException, T> right(to)
            : Either.<ApplicationException, DocumentNode> right(documents.getFirst())
                .flatMap(DocumentFactory::build)
                .map(to::withDocument));
  }

  static <T extends HasDocumentOptional<T>, F extends HasDocumentNodeList<F>> Function<T, Either<ApplicationException, T>> addDocumentStrict(F from) {
    return (T to) -> Either.<ApplicationException, F> right(from)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("From Object can not be null!")))
        .map(F::getDoucments)
        .flatMap(EITHER.<List<DocumentNode>> makeSure(
            documents -> documents.size() == 1,
            documents -> new InvalidValueException("Exactly one Document expected, got " + documents.size() + "!")))
        .map(List::getFirst)
        .flatMap(DocumentFactory::build)
        .map(to::withDocument);
  }
}

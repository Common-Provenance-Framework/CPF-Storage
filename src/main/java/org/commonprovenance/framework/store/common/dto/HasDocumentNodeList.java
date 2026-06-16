package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.DocumentNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.Owns;

import io.vavr.control.Either;

public interface HasDocumentNodeList<T extends HasDocumentNodeList<T>> {
  List<DocumentNode> getDoucments();

  List<Owns> getOwns();

  T withDocument(DocumentNode dcoumentNode);

  T withOwns(List<Owns> owns);

  static <T extends HasDocumentNodeList<T>, F extends HasDocumentOptional<F>> Function<T, Either<ApplicationException, T>> addDocument(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .flatMap(EITHER.liftEitherOptional(F::getDocument))
        .map(DocumentNodeFactory::build)
        .map(to::withDocument);
  }

  static <T extends HasDocumentNodeList<T>, F extends HasDocumentOptional<F>> Function<T, Either<ApplicationException, T>> addDocumentWithRelations(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .flatMap(EITHER.liftEitherOptional(F::getDocument))
        .map(DocumentNodeFactory::buildWithRelations)
        .map(to::withDocument);
  }

  static <T extends HasDocumentNodeList<T>, F extends HasDocumentOptional<F>> Function<T, Either<ApplicationException, T>> addDocumentWithFullRelations(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .flatMap(EITHER.liftEitherOptional(F::getDocument))
        .map(DocumentNodeFactory::buildWithFullRelations)
        .map(to::withDocument);
  }

  // ---

  static <T extends HasDocumentNodeList<T>, F extends HasDocument<F>> Function<T, Either<ApplicationException, T>> addDocument(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .map(F::getDocument)
        .map(DocumentNodeFactory::build)
        .map(to::withDocument);
  }

  static <T extends HasDocumentNodeList<T>, F extends HasDocument<F>> Function<T, Either<ApplicationException, T>> addDocumentWithRelations(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .map(F::getDocument)
        .map(DocumentNodeFactory::buildWithRelations)
        .map(to::withDocument);
  }

  static <T extends HasDocumentNodeList<T>, F extends HasDocument<F>> Function<T, Either<ApplicationException, T>> addDocumentWithFullRelations(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .map(F::getDocument)
        .map(DocumentNodeFactory::buildWithFullRelations)
        .map(to::withDocument);
  }
}

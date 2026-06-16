package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Format;

import io.vavr.control.Either;

public interface HasFormat<T extends HasFormat<T>> {
  Format getFormat();

  default T withFormat(Format format) {
    throw new InternalApplicationException("withFormat is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  static <T extends HasFormat<T>, F extends HasFormat<F>> UnaryOperator<T> addFormat(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getFormat)
        .map(to::withFormat)
        .orElse(to);
  }

  static <T extends HasFormat<T>, F extends HasFormatSerialized<F>> Function<T, Either<ApplicationException, T>> addFormat(F from) {
    return (T to) -> Either.<ApplicationException, F> right(from)
        .flatMap(EITHER.makeSureNotNull(x -> new InvalidValueException("From Object can not be null!")))
        .map(F::getDocumentFormat)
        .flatMap(EITHER.liftEitherOptional(Format::from))
        .map(to::withFormat);
  }

  static <T extends HasFormat<T>, F> UnaryOperator<T> addFormatIfPresent(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(HasFormat::getValue)
        .map(to::withFormat)
        .orElse(to);
  }

  private static <T> Optional<Format> getValue(T form) {
    if (form instanceof HasFormat<?> has)
      return Optional.of(has.getFormat());

    if (form instanceof HasFormatSerialized<?> has)
      return Optional.of(has.getDocumentFormat()).flatMap(Format::from);

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasFormat has)
      return Optional.of(has.getFormat()).flatMap(Format::from);

    return Optional.empty();
  }

}

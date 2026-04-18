package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.model.interop.InteropMediaType;

import io.vavr.control.Either;

public interface ProvDocumentUtils {

  public final Charset charset = java.nio.charset.StandardCharsets.UTF_8;

  ProvDocumentFunctionalUtils FUNCTIONAL = new ProvDocumentFunctionalUtils();
  ProvDocumentImperativeUtils IMPERATIVE = new ProvDocumentImperativeUtils();

  class ProvDocumentFunctionalUtils {

    public Function<Document, Either<ApplicationException, String>> serialize(
        Formats.ProvFormat format) {

      Function<Document, Function<String, Either<ApplicationException, String>>> provToString = (
          Document d) -> (intermediaType) -> {
            try {
              InteropFramework interop = new InteropFramework();
              ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
              interop.writeDocument(outputStream, d, intermediaType, false);
              return Either.right(outputStream.toString(ProvDocumentUtils.charset));
            } catch (Throwable throwable) {
              return Either.left(new InternalApplicationException(
                  "Can not write document to string: " + throwable.getMessage(), throwable));
            }
          };

      return (Document document) -> Either.<ApplicationException, Formats.ProvFormat>right(format)
          .flatMap(this::provFormatToIntermediaType)
          .flatMap(provToString.apply(document))
          .flatMap(EITHER.makeSureBefore(
              format == Formats.ProvFormat.JSON,
              ProvJsonUtils.FUNCTIONAL.postprocessJsonAfterSerialization));

    }

    public Function<String, Either<ApplicationException, Document>> deserialize(
        Formats.ProvFormat format) {

      Function<Formats.ProvFormat, Function<String, Either<ApplicationException, Document>>> stringToProv = (
          Formats.ProvFormat provFormat) -> (String document) -> {
            try {
              InteropFramework interop = new InteropFramework();
              InputStream inputStream = new ByteArrayInputStream(BytesUtils.stringToBytes_UTF8(document));
              return Either.right(interop.readDocument(inputStream, provFormat));
            } catch (Throwable throwable) {
              return Either.left(new InternalApplicationException(
                  "Can not read document from string: " + throwable.getMessage(), throwable));
            }
          };

      return (String document) -> Either.<ApplicationException, String>right(document)
          .flatMap(EITHER.makeSureBefore(
              format == Formats.ProvFormat.JSON,
              ProvJsonUtils.FUNCTIONAL.preprocessJsonForDeserialization))
          .flatMap(stringToProv.apply(format));

    }

    private Either<ApplicationException, String> provFormatToIntermediaType(Formats.ProvFormat format) {
      return switch (format) {
        case JSON -> Either.right(InteropMediaType.MEDIA_APPLICATION_JSON);
        case JSONLD -> Either.right(InteropMediaType.MEDIA_APPLICATION_JSONLD);
        case PROVN -> Either.right(InteropMediaType.MEDIA_TEXT_PROVENANCE_NOTATION);
        case TURTLE -> Either.right(InteropMediaType.MEDIA_TEXT_TURTLE);
        case TRIG -> Either.right(InteropMediaType.MEDIA_APPLICATION_TRIG);
        case RDFXML -> Either.right(InteropMediaType.MEDIA_APPLICATION_XML);
        default ->
          Either.left(new InvalidValueException("InteropMediaType for ProvFormat '" + format + "' is not defined."));
      };
    }

  }

  class ProvDocumentImperativeUtils {
    public String serialize(Document document, Formats.ProvFormat format) throws ApplicationException {
      return FUNCTIONAL.serialize(format)
          .apply(document)
          .getOrElseThrow(Function.identity());
    }

    public Document deserialize(String document, Formats.ProvFormat format) throws ApplicationException {
      return FUNCTIONAL.deserialize(format)
          .apply(document)
          .getOrElseThrow(Function.identity());
    }
  }
}

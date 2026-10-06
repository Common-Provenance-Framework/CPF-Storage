package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.HashFunction;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.model.interop.InteropMediaType;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.model.CpmUtilities;
import io.vavr.control.Either;

public final class ProvDocumentUtils {
  public final static Charset charset = java.nio.charset.StandardCharsets.UTF_8;

  public static Function<Document, Either<ApplicationException, String>> serialize(Formats.ProvFormat format) {

    Function<Document, Function<String, Either<ApplicationException, String>>> provToString = document -> intermediaType -> {
      try {
        InteropFramework interop = new InteropFramework();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        interop.writeDocument(outputStream, document, intermediaType, false);
        return Either.right(outputStream.toString(ProvDocumentUtils.charset));
      } catch (Throwable throwable) {
        return Either.left(new InternalApplicationException(
            "Can not write document to string: " + throwable.getMessage(), throwable));
      }
    };

    return (Document document) -> Either.<ApplicationException, Formats.ProvFormat> right(format)
        .flatMap(ProvDocumentUtils::provFormatToIntermediaType)
        .flatMap(provToString.apply(document));

  }

  public static Either<ApplicationException, Document> deserialize(String document, Formats.ProvFormat format) {
    return ProvDocumentUtils.deserialize(format).apply(document);
  }

  public static Function<String, Either<ApplicationException, Document>> deserialize(Formats.ProvFormat format) {

    Function<Formats.ProvFormat, Function<String, Either<ApplicationException, Document>>> stringToProv = (Formats.ProvFormat provFormat) -> (String document) -> {
      return EITHER.<InputStream, InteropFramework, Document> combineChecked(
          Either.<ApplicationException, String> right(document)
              .flatMap(BytesUtils::stringToBytes_UTF8)
              .flatMap(EITHER.liftEither(bs -> new ByteArrayInputStream(bs))),
          EITHER.liftEither(() -> new InteropFramework()),
          (InputStream is, InteropFramework interop) -> interop.readDocument(is, format));
    };

    return (String document) -> Either.<ApplicationException, String> right(document)
        .flatMap(stringToProv.apply(format));

  }

  public static Either<ApplicationException, QualifiedName> getCpmReferencedMetaBundleId(HasOther hasOther) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(ProvDocumentUtils.getCpmAttributeValueQN(CpmAttribute.REFERENCED_META_BUNDLE_ID));
  }

  public static Either<ApplicationException, QualifiedName> getCpmReferencedBundleId(HasOther hasOther) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(ProvDocumentUtils.getCpmAttributeValueQN(CpmAttribute.REFERENCED_BUNDLE_ID));
  }

  public static Either<ApplicationException, String> getCpmHashAlgAsString(HasOther hasOther) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(ProvDocumentUtils.getCpmAttributeValueString(CpmAttribute.HASH_ALG));
  }

  public static Either<ApplicationException, HashFunction> getCpmHashAlg(HasOther hasOther) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(ProvDocumentUtils.getCpmAttributeValueString(CpmAttribute.HASH_ALG))
        .flatMap(EITHER.liftEitherOptional(
            HashFunction::from,
            hashFunction -> new InvalidValueException("Unknown hash function: '" + hashFunction + "'")));
  }

  public static Either<ApplicationException, String> getCpmReferencedBundleHashValue(HasOther hasOther) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(ProvDocumentUtils.getCpmAttributeValueString(CpmAttribute.HASH_VALUE));
  }

  private static Either<ApplicationException, String> provFormatToIntermediaType(Formats.ProvFormat format) {
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

  private static Function<HasOther, Either<ApplicationException, QualifiedName>> getCpmAttributeValueQN(CpmAttribute attribute) {
    return (HasOther hasOther) -> ProvDocumentUtils.getCpmAttributeValue(hasOther, attribute)
        .flatMap(EITHER.makeSure(
            QualifiedName.class::isInstance,
            attribute.toString() + " value is not instance of QualifiedName!"))
        .map(QualifiedName.class::cast);
  }

  private static Function<HasOther, Either<ApplicationException, String>> getCpmAttributeValueString(CpmAttribute attribute) {
    return (HasOther hasOther) -> ProvDocumentUtils.getCpmAttributeValue(hasOther, attribute)
        .flatMap(EITHER.makeSure(
            String.class::isInstance,
            attribute.toString() + " value is not instance of String!"))
        .map(String.class::cast);
  }

  private static Either<ApplicationException, Object> getCpmAttributeValue(HasOther hasOther, CpmAttribute attribute) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(EITHER.<HasOther> makeSureNotNullWithMessage("Statement can not be null!"))
        .map(statement -> CpmUtilities.getCpmAttributeValue(statement, attribute))
        .flatMap(EITHER.makeSureNotNullWithMessage("Statement does not have '" + attribute.toString() + "' attribute, or its value is null!"));
  }

}

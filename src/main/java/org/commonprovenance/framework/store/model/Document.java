package org.commonprovenance.framework.store.model;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasGraph;
import org.commonprovenance.framework.store.common.dto.HasOptionalFormat;
import org.commonprovenance.framework.store.common.dto.HasOptionalIdentifier;
import org.commonprovenance.framework.store.common.dto.HasOrganizationIdentifier;
import org.commonprovenance.framework.store.common.dto.HasSignature;
import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.interop.Formats;

import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import io.vavr.Function1;
import io.vavr.control.Either;

public class Document implements HasOptionalIdentifier,
    HasOrganizationIdentifier<Document>, HasOptionalFormat, HasSignature<Document>, HasGraph<Document> {
  private final Optional<String> identifier;
  private final String organizationIdentifier;
  private final String graph;
  private final Optional<Format> format;
  private final String signature;

  private final Optional<CpmDocument> cpmDocument;
  private final Optional<Token> token;

  public Document(
      String identifier,
      String organizationIdentifier,
      String graph,
      Format format,
      String signature) {
    this.identifier = Optional.ofNullable(identifier);
    this.organizationIdentifier = organizationIdentifier;
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;
    this.cpmDocument = Optional.empty();
    this.token = Optional.empty();
  }

  public Document(
      String identifier,
      String organizationIdentifier,
      String graph,
      Format format,
      String signature,
      CpmDocument cpmDocument,
      Token token) {
    this.identifier = Optional.ofNullable(identifier);
    this.organizationIdentifier = organizationIdentifier;
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;

    this.cpmDocument = Optional.ofNullable(cpmDocument);
    this.token = Optional.ofNullable(token);
  }

  public Document withIdentifier(String identifier) {
    return new Document(
        identifier,
        this.getOrganizationIdentifier(),
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Document withOrganizationIdentifier(String organizationIdentifier) {
    return new Document(
        this.getIdentifier().orElse(null),
        organizationIdentifier,
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Document withGraph(String graph) {
    return new Document(
        this.getIdentifier().orElse(null),
        this.getOrganizationIdentifier(),
        graph,
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Document withFormat(Format format) {
    return new Document(
        this.getIdentifier().orElse(null),
        this.getOrganizationIdentifier(),
        this.getGraph(),
        format,
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Either<ApplicationException, Document> withCpmDocument(
      ProvFactory provFactory,
      ICpmProvFactory cpmProvFactory,
      ICpmFactory cpmFactory) {

    return EITHER.combineM(
        Either.<ApplicationException, String> right(this.graph)
            .flatMap(Base64Utils::decodeToString),
        EITHER.liftEither(this.format)
            .flatMap(EITHER.<Format, Formats.ProvFormat> liftEither(Format::toProvFormat))
            .mapLeft(ApplicationExceptionFactory.build(InvalidValueException::new, "Unknown Graph format!")),
        ProvDocumentUtils::deserialize)
        .map(this.cpmFactory(provFactory, cpmProvFactory, cpmFactory))
        .map((CpmDocument cpmDocument) -> new Document(
            this.getIdentifier().orElse(null),
            this.getOrganizationIdentifier(),
            this.getGraph(),
            this.getFormat().orElse(null),
            this.getSignature(),
            cpmDocument,
            this.getToken().orElse(null)))
        .mapLeft(ApplicationExceptionFactory.build(InternalApplicationException::new, "Graf has not been deserialized"));
  }

  public Document withToken(Token token) {
    return new Document(
        this.getIdentifier().orElse(null),
        this.getOrganizationIdentifier(),
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        token);
  }

  public Document withSignature(String signature) {
    return new Document(
        this.getIdentifier().orElse(null),
        this.getOrganizationIdentifier(),
        this.getGraph(),
        this.getFormat().orElse(null),
        signature,
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  private Function1<org.openprovenance.prov.model.Document, CpmDocument> cpmFactory(
      ProvFactory provFactory,
      ICpmProvFactory cpmProvFactory,
      ICpmFactory cpmFactory) {
    return (org.openprovenance.prov.model.Document document) -> {
      return new CpmDocument(document, provFactory, cpmProvFactory, cpmFactory);
    };
  }

  public Optional<String> getIdentifier() {
    return identifier;
  }

  public String getOrganizationIdentifier() {
    return organizationIdentifier;
  }

  public String getGraph() {
    return graph;
  }

  public Optional<Format> getFormat() {
    return format;
  }

  public String getSignature() {
    return signature;
  }

  public Optional<CpmDocument> getCpmDocument() {
    return cpmDocument;
  }

  public Optional<Token> getToken() {
    return token;
  }

  @Override
  public String toString() {
    return "Document [identifier=" + identifier + ", organizationIdentifier=" + organizationIdentifier + ", graph="
        + graph + ", format=" + format + ", signature=" + signature + ", cpmDocument=" + cpmDocument + ", token="
        + token + "]";
  }

}

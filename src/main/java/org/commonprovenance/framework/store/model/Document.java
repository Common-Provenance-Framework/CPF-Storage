package org.commonprovenance.framework.store.model;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasCpmDocument;
import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasGraph;
import org.commonprovenance.framework.store.common.dto.HasTokenOptional;
import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.common.validation.DTOValidator;
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

public class Document extends DTOValidator implements
    HasCpmDocument<Document>,
    HasGraph<Document>,
    HasFormat<Document>,
    HasTokenOptional<Document> {
  private final String graph;
  private final Format format;

  private final Optional<CpmDocument> cpmDocument;
  private final Optional<Token> token;

  public Document(
      String graph,
      Format format) {
    this.graph = graph;
    this.format = format;
    this.cpmDocument = Optional.empty();
    this.token = Optional.empty();
  }

  public Document(
      String graph,
      Format format,
      CpmDocument cpmDocument,
      Token token) {
    this.graph = graph;
    this.format = format;

    this.cpmDocument = Optional.ofNullable(cpmDocument);
    this.token = Optional.ofNullable(token);
  }

  public Document() {
    this.graph = null;
    this.format = null;

    this.cpmDocument = Optional.empty();
    this.token = Optional.empty();
  }

  public Document withGraph(String graph) {
    return new Document(
        graph,
        this.getFormat(),
        this.cpmDocument.orElse(null),
        this.getToken().orElse(null));
  }

  public Document withFormat(Format format) {
    return new Document(
        this.getGraph(),
        format,
        this.cpmDocument.orElse(null),
        this.getToken().orElse(null));
  }

  public Either<ApplicationException, Document> withCpmDocument(
      ProvFactory provFactory,
      ICpmProvFactory cpmProvFactory,
      ICpmFactory cpmFactory) {

    return EITHER.combineM(
        Either.<ApplicationException, String> right(this.graph)
            .flatMap(Base64Utils::decodeToString),
        Either.<ApplicationException, Format> right(this.format)
            .flatMap(EITHER.<Format, Formats.ProvFormat> liftEither(Format::toProvFormat))
            .mapLeft(ApplicationExceptionFactory.build(InvalidValueException::new, "Unknown Graph format!")),
        ProvDocumentUtils::deserialize)
        .map(this.cpmFactory(provFactory, cpmProvFactory, cpmFactory))
        .map((CpmDocument cpmDocument) -> new Document(
            this.getGraph(),
            this.getFormat(),
            cpmDocument,
            this.getToken().orElse(null)))
        .mapLeft(ApplicationExceptionFactory.build(InternalApplicationException::new, "Graf has not been deserialized"));
  }

  public Document withToken(Token token) {
    return new Document(
        this.getGraph(),
        this.getFormat(),
        this.cpmDocument.orElse(null),
        token);
  }

  private Function1<org.openprovenance.prov.model.Document, CpmDocument> cpmFactory(
      ProvFactory provFactory,
      ICpmProvFactory cpmProvFactory,
      ICpmFactory cpmFactory) {
    return (org.openprovenance.prov.model.Document document) -> {
      return new CpmDocument(document, provFactory, cpmProvFactory, cpmFactory);
    };
  }

  public String getGraph() {
    return graph;
  }

  public Format getFormat() {
    return format;
  }

  @Override
  public Either<ApplicationException, CpmDocument> getCpmDocument() {
    return EITHER.liftEither(
        cpmDocument,
        new InvalidValueException("CpmDocument has not been deserialized yet"));
  }

  public Optional<Token> getToken() {
    return token;
  }

}

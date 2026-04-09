package org.commonprovenance.framework.store.model;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.common.dto.HasGraph;
import org.commonprovenance.framework.store.common.dto.HasOptionalFormat;
import org.commonprovenance.framework.store.common.dto.HasOptionalIdentifier;
import org.commonprovenance.framework.store.common.dto.HasOrganizationIdentifier;
import org.commonprovenance.framework.store.common.dto.HasSignature;
import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.interop.Formats;

import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;

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

  public Document withCpmDocument(ProvFactory provFactory, ICpmProvFactory cpmProvFactory, ICpmFactory cpmFactory) {
    return this.withCpmDocument(provFactory, cpmProvFactory, cpmFactory, false);
  }

  public Document withCpmDocument(ProvFactory provFactory, ICpmProvFactory cpmProvFactory, ICpmFactory cpmFactory,
      Boolean force) {
    return this.cpmDocument.isPresent() && !force
        ? this
        : Optional.ofNullable(this.graph)
            .map(Base64Utils::decodeToString)
            .flatMap(
                (String json) -> {
                  try {
                    return Optional.of(ProvDocumentUtils.deserialize(
                        json,
                        this.format.map(Format::toProvFormat).orElse(Formats.ProvFormat.JSON)));
                  } catch (IOException e) {
                    System.err.println("Can not deserialize document!");
                    return Optional.empty();
                  }
                })
            .map(this.cpmFactory(provFactory, cpmProvFactory, cpmFactory))
            .map((CpmDocument cpmDocument) -> new Document(
                this.getIdentifier().orElse(null),
                this.getOrganizationIdentifier(),
                this.getGraph(),
                this.getFormat().orElse(null),
                this.getSignature(),
                cpmDocument,
                this.getToken().orElse(null)))
            .orElse(this);
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

  private Function<org.openprovenance.prov.model.Document, CpmDocument> cpmFactory(
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

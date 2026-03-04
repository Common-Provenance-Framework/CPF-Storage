package org.commonprovenance.framework.store.model;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.interop.Formats;

import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;

public class Document {
  private final String id;
  private final UUID organizationId;
  private final String organizationName;
  private final String graph;
  private final Optional<Format> format;
  private final String signature;

  private final Optional<CpmDocument> cpmDocument;
  private final Optional<Token> token;

  public Document(String id, UUID organizationId, String organizationName, String graph, Format format,
      String signature) {
    this.id = id;
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;
    this.cpmDocument = Optional.empty();
    this.token = Optional.empty();
  }

  public Document(String id, UUID organizationId,
      String organizationName, String graph, Format format, String signature,
      CpmDocument cpmDocument, Token token) {
    this.id = id;
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;

    this.cpmDocument = Optional.ofNullable(cpmDocument);
    this.token = Optional.ofNullable(token);
  }

  public Document withId(String id) {
    return new Document(
        id,
        this.getOrganizationId(),
        this.getOrganizationName(),
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Document withOrganizationId(UUID organizationId) {
    return new Document(
        this.getId(),
        organizationId,
        this.getOrganizationName(),
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Document withOrganizationName(String organizationName) {
    return new Document(
        this.getId(),
        this.getOrganizationId(),
        organizationName,
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Document withFormat(Format format) {
    return new Document(
        this.getId(),
        this.getOrganizationId(),
        this.getOrganizationName(),
        this.getGraph(),
        format,
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        this.getToken().orElse(null));
  }

  public Document withCpmDocument(ProvFactory provFactory, ICpmProvFactory cpmProvFactory, ICpmFactory cpmFactory) {
    return this.cpmDocument.isPresent()
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
                this.getId(),
                this.getOrganizationId(),
                this.getOrganizationName(),
                this.getGraph(),
                this.getFormat().orElse(null),
                this.getSignature(),
                cpmDocument,
                this.getToken().orElse(null)))
            .orElse(this);
  }

  public Document withToken(Token token) {
    return new Document(
        this.getId(),
        this.getOrganizationId(),
        this.getOrganizationName(),
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature(),
        this.getCpmDocument().orElse(null),
        token);
  }

  private Function<org.openprovenance.prov.model.Document, CpmDocument> cpmFactory(
      ProvFactory provFactory,
      ICpmProvFactory cpmProvFactory,
      ICpmFactory cpmFactory) {
    return (org.openprovenance.prov.model.Document document) -> {
      return new CpmDocument(document, provFactory, cpmProvFactory, cpmFactory);
    };
  }

  public String getId() {
    return id;
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public String getOrganizationName() {
    return organizationName;
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
}

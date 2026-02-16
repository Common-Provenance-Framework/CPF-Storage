package org.commonprovenance.framework.store.model;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.interop.Formats;

import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;

public class Document {
  private final Optional<UUID> id;
  private final UUID organizationId;
  private final String graph;
  private final Optional<Format> format;
  private final String signature;

  private final Optional<CpmDocument> cpmDocument;

  public Document(UUID id, UUID organizationId, String graph, Format format, String signature) {
    this.id = Optional.ofNullable(id);
    this.organizationId = organizationId;
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;
    this.cpmDocument = Optional.empty();
  }

  public Document(UUID id, UUID organizationId, String graph, Format format, String signature,
      CpmDocument cpmDocument) {
    this.id = Optional.ofNullable(id);
    this.organizationId = organizationId;
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;
    this.cpmDocument = Optional.ofNullable(cpmDocument);
  }

  public Document withId(UUID id) {
    return new Document(
        id,
        this.getOrganizationId(),
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature());
  }

  public Document withOrganizationId(UUID organizationId) {
    return new Document(
        this.getId().orElse(null),
        organizationId,
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature());
  }

  public Document withFormat(Format format) {
    return new Document(
        this.getId().orElse(null),
        this.getOrganizationId(),
        this.getGraph(),
        format,
        this.getSignature());
  }

  public Document withCpmDocument(ProvFactory provFactory, ICpmProvFactory cpmProvFactory, ICpmFactory cpmFactory) {
    return this.cpmDocument.isPresent()
        ? this
        : Optional.ofNullable(this.graph)
            .map(g -> Base64Utils.decodeToString(g))
            .flatMap(
                json -> {
                  try {
                    return Optional
                        .of(ProvDocumentUtils.deserialize(
                            json,
                            this.format.map(Format::toProvFormat).orElse(Formats.ProvFormat.JSON)));
                  } catch (IOException e) {
                    System.err.println("Can not deserialize document!");
                    return Optional.empty();
                  }
                })
            .map(d -> new CpmDocument(d, provFactory, cpmProvFactory, cpmFactory))
            .map(cd -> new Document(
                this.getId().orElse(null),
                this.getOrganizationId(),
                this.getGraph(),
                this.getFormat().orElse(null),
                this.getSignature(), cd))
            .orElse(this);
  }

  public Optional<UUID> getId() {
    return id;
  }

  public UUID getOrganizationId() {
    return organizationId;
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
}

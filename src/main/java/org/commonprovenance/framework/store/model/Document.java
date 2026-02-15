package org.commonprovenance.framework.store.model;

import java.util.Optional;
import java.util.UUID;

public class Document {
  private final Optional<UUID> id;
  private final UUID organizationId;
  private final String graph;
  private final Optional<Format> format;
  private final String signature;

  public Document(UUID id, UUID organizationId, String graph, Format format, String signature) {
    this.id = Optional.ofNullable(id);
    this.organizationId = organizationId;
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;
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
}

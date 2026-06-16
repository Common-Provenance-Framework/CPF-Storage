package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.time.Instant;

import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasDocumentGraph;
import org.commonprovenance.framework.store.common.dto.HasFormatSerialized;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.dto.HasSignature;
import org.commonprovenance.framework.store.common.dto.HasTokenFormat;
import org.commonprovenance.framework.store.common.dto.HasType;
import org.commonprovenance.framework.store.common.validation.ValidatableDTO;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.GraphType;

public class IssueTokenTPFormDTO extends ValidatableDTO implements
    HasOrganizationId<IssueTokenTPFormDTO>,
    HasDocumentGraph<IssueTokenTPFormDTO>,
    HasFormatSerialized<IssueTokenTPFormDTO>,
    HasSignature<IssueTokenTPFormDTO>,
    HasType<IssueTokenTPFormDTO>,
    HasCreatedOn<IssueTokenTPFormDTO>,
    HasTokenFormat<IssueTokenTPFormDTO> {
  private final String organizationId;
  private final String document;
  private final String documentFormat;
  private final String signature;
  private final String type;
  private final Long createdOn;
  private final String tokenFormat;

  public IssueTokenTPFormDTO() {
    this.organizationId = null;
    this.document = null;
    this.documentFormat = null;
    this.signature = null;
    this.type = null;
    this.createdOn = Instant.now().getEpochSecond();
    this.tokenFormat = "jwt";
  }

  public IssueTokenTPFormDTO(
      String organizationId,
      String document,
      String documentFormat,
      String signature,
      String type,
      Long createdOn,
      String tokenFormat) {
    this.organizationId = organizationId;
    this.document = document;
    this.documentFormat = documentFormat;
    this.signature = signature;
    this.type = type;
    this.createdOn = createdOn;
    this.tokenFormat = tokenFormat != null ? tokenFormat : "jwt";
  }

  @Override
  public IssueTokenTPFormDTO withOrganizationId(String identifier) {
    return new IssueTokenTPFormDTO(
        identifier,
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getType(),
        this.getCreatedOn(),
        this.getTokenFormat());
  }

  @Override
  public IssueTokenTPFormDTO withDocument(String document) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        document,
        this.getDocumentFormat(),
        this.getSignature(),
        this.getType(),
        this.getCreatedOn(),
        this.getTokenFormat());
  }

  @Override
  public IssueTokenTPFormDTO withDocumentFormat(Format format) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        format.toString().toLowerCase(),
        this.getSignature(),
        this.getType(),
        this.getCreatedOn(),
        this.getTokenFormat());
  }

  @Override
  public IssueTokenTPFormDTO withSignature(String signature) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        signature,
        this.getType(),
        this.getCreatedOn(),
        this.getTokenFormat());
  }

  public IssueTokenTPFormDTO withType(GraphType graphType) {
    return this.withType(graphType.toString().toLowerCase());
  }

  @Override
  public IssueTokenTPFormDTO withType(String type) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        type,
        this.getCreatedOn(),
        this.getTokenFormat());
  }

  @Override
  public IssueTokenTPFormDTO withCreatedOn(Long createdOn) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getType(),
        createdOn,
        this.getTokenFormat());
  }

  @Override
  public IssueTokenTPFormDTO withTokenFormat(String tokenFormat) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getType(),
        this.getCreatedOn(),
        tokenFormat);
  }

  @Override
  public String getOrganizationId() {
    return organizationId;
  }

  @Override
  public String getDocument() {
    return document;
  }

  @Override
  public String getDocumentFormat() {
    return documentFormat;
  }

  @Override
  public String getSignature() {
    return signature;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Long getCreatedOn() {
    return createdOn;
  }

  @Override
  public String getTokenFormat() {
    return tokenFormat;
  }

}

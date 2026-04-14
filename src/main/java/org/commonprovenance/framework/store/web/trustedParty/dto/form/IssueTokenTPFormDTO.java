package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.time.Instant;

import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasDocument;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.dto.HasSignature;
import org.commonprovenance.framework.store.common.validation.ValidatableDTO;

public class IssueTokenTPFormDTO extends ValidatableDTO
    implements HasOrganizationId<IssueTokenTPFormDTO>, HasDocument<IssueTokenTPFormDTO>,
    HasSignature<IssueTokenTPFormDTO>, HasCreatedOn<IssueTokenTPFormDTO> {
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
  public IssueTokenTPFormDTO withOrganizationId(String organizationId) {
    return new IssueTokenTPFormDTO(
        organizationId,
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

  public IssueTokenTPFormDTO withDocumentFormat(String format) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        format,
        this.getSignature(),
        this.getType(),
        this.getCreatedOn(),
        this.getTokenFormat());
  }

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

  public IssueTokenTPFormDTO withGraphType(String type) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        type,
        this.getCreatedOn(),
        this.getTokenFormat());
  }

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

  public String getDocument() {
    return document;
  }

  public String getDocumentFormat() {
    return documentFormat;
  }

  public String getSignature() {
    return signature;
  }

  public String getType() {
    return type;
  }

  public Long getCreatedOn() {
    return createdOn;
  }

  public String getTokenFormat() {
    return tokenFormat;
  }
}

package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasDocument;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.validation.ValidatableDTO;

public class IssueTokenTPFormDTO extends ValidatableDTO
    implements HasOrganizationId<IssueTokenTPFormDTO>, HasDocument<IssueTokenTPFormDTO>,
    HasCreatedOn<IssueTokenTPFormDTO> {
  private final String organizationId;
  private final String document;
  private final String documentFormat;
  private final String signature;
  private final String type;
  private final Long createdOn;

  public IssueTokenTPFormDTO() {
    this.organizationId = null;
    this.document = null;
    this.documentFormat = null;
    this.signature = null;
    this.type = null;
    this.createdOn = System.currentTimeMillis();
  }

  public IssueTokenTPFormDTO(
      String organizationId,
      String document,
      String documentFormat,
      String signature,
      String type,
      Long createdOn) {
    this.organizationId = organizationId;
    this.document = document;
    this.documentFormat = documentFormat;
    this.signature = signature;
    this.type = type;
    this.createdOn = createdOn;
  }

  @Override
  public IssueTokenTPFormDTO withOrganizationId(String id) {
    return new IssueTokenTPFormDTO(
        id,
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getType(),
        this.getCreatedOn());
  }

  @Override
  public IssueTokenTPFormDTO withDocument(String graph) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        graph,
        this.getDocumentFormat(),
        this.getSignature(),
        this.getType(),
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withDocumentFormat(String format) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        format,
        this.getSignature(),
        this.getType(),
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withSignature(String signature) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        signature,
        this.getType(),
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withGraphType(String type) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        type,
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withCreatedOn(Long createdOn) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getType(),
        createdOn);
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
}

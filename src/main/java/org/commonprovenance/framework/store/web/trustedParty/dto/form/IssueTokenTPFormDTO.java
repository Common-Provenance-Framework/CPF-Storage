package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import org.commonprovenance.framework.store.common.validation.ValidatableDTO;

public class IssueTokenTPFormDTO extends ValidatableDTO {
  private final String organizationId;
  private final String document;
  private final String documentFormat;
  private final String type;
  private final String createdOn;

  public IssueTokenTPFormDTO() {
    this.organizationId = null;
    this.document = null;
    this.documentFormat = null;
    this.type = null;
    this.createdOn = null;
  }

  public IssueTokenTPFormDTO(
      String organizationId,
      String document,
      String documentFormat,
      String type,
      String createdOn) {
    this.organizationId = organizationId;
    this.document = document;
    this.documentFormat = documentFormat;
    this.type = type;
    this.createdOn = createdOn;
  }

  public IssueTokenTPFormDTO withOrganizationId(String id) {
    return new IssueTokenTPFormDTO(
        id,
        this.getDocument(),
        this.getDocumentFormat(),
        this.getType(),
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withDocument(String graph) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        graph,
        this.getDocumentFormat(),
        this.getType(),
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withDocumentFormat(String format) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        format,
        this.getType(),
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withGraphType(String type) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        type,
        this.getCreatedOn());
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public String getDocument() {
    return document;
  }

  public String getDocumentFormat() {
    return documentFormat;
  }

  public String getType() {
    return type;
  }

  public String getCreatedOn() {
    return createdOn;
  }
}

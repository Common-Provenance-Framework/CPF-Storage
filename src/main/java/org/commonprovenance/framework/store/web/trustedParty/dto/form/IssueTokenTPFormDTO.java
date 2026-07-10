package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.time.Instant;

import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasDocumentGraph;
import org.commonprovenance.framework.store.common.dto.HasDocumentType;
import org.commonprovenance.framework.store.common.dto.HasFormatSerialized;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.dto.HasSignature;
import org.commonprovenance.framework.store.common.validation.DTOValidator;
import org.commonprovenance.framework.store.model.DocumentType;
import org.commonprovenance.framework.store.model.Format;

public class IssueTokenTPFormDTO extends DTOValidator implements
    HasOrganizationId<IssueTokenTPFormDTO>,
    HasDocumentGraph<IssueTokenTPFormDTO>,
    HasFormatSerialized<IssueTokenTPFormDTO>,
    HasSignature<IssueTokenTPFormDTO>,
    HasDocumentType<IssueTokenTPFormDTO>,
    HasCreatedOn<IssueTokenTPFormDTO> {
  private final String organizationId;
  private final String document;
  private final String documentFormat;
  private final String signature;
  private final String documentType;
  private final Long createdOn;

  public IssueTokenTPFormDTO() {
    this.organizationId = null;
    this.document = null;
    this.documentFormat = null;
    this.signature = null;
    this.documentType = null;
    this.createdOn = Instant.now().getEpochSecond();
  }

  public IssueTokenTPFormDTO(
      String organizationId,
      String document,
      String documentFormat,
      String signature,
      String documentType,
      Long createdOn) {
    this.organizationId = organizationId;
    this.document = document;
    this.documentFormat = documentFormat;
    this.signature = signature;
    this.documentType = documentType;
    this.createdOn = createdOn;
  }

  @Override
  public IssueTokenTPFormDTO withOrganizationId(String identifier) {
    return new IssueTokenTPFormDTO(
        identifier,
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getDocumentType(),
        this.getCreatedOn());
  }

  @Override
  public IssueTokenTPFormDTO withDocument(String document) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        document,
        this.getDocumentFormat(),
        this.getSignature(),
        this.getDocumentType(),
        this.getCreatedOn());
  }

  @Override
  public IssueTokenTPFormDTO withDocumentFormat(Format format) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        format.toString().toLowerCase(),
        this.getSignature(),
        this.getDocumentType(),
        this.getCreatedOn());
  }

  @Override
  public IssueTokenTPFormDTO withSignature(String signature) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        signature,
        this.getDocumentType(),
        this.getCreatedOn());
  }

  public IssueTokenTPFormDTO withType(DocumentType graphType) {
    return this.withDocumentType(graphType.toString().toLowerCase());
  }

  @Override
  public IssueTokenTPFormDTO withDocumentType(String documentType) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        documentType,
        this.getCreatedOn());
  }

  @Override
  public IssueTokenTPFormDTO withCreatedOn(Long createdOn) {
    return new IssueTokenTPFormDTO(
        this.getOrganizationId(),
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getDocumentType(),
        createdOn);
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
  public String getDocumentType() {
    return documentType;
  }

  @Override
  public Long getCreatedOn() {
    return createdOn;
  }

}

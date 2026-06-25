package org.commonprovenance.framework.store.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasDocumentOptional;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dto.HasTrustedPartyOptional;
import org.commonprovenance.framework.store.common.validation.DTOValidator;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;

import io.vavr.control.Either;

public class Organization extends DTOValidator implements
    HasIdentifier<Organization>,
    HasClientCertificate<Organization>,
    HasIntermediateCertificates<Organization>,
    HasTrustedPartyOptional<Organization>,
    HasDocumentOptional<Organization> {
  private final String identifier;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;
  private final Optional<TrustedParty> trustedParty;
  private final Optional<Document> document;

  public Organization(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates,
      TrustedParty trustedParty,
      Document document) {
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
    this.trustedParty = Optional.ofNullable(trustedParty);
    this.document = Optional.ofNullable(document);
  }

  public Organization(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
    this.trustedParty = Optional.empty();
    this.document = Optional.empty();
  }

  public Organization() {
    this.identifier = null;
    this.clientCertificate = null;
    this.intermediateCertificates = Collections.emptyList();
    this.trustedParty = Optional.empty();
    this.document = Optional.empty();
  }

  public Organization withIdentifier(String identifier) {
    return new Organization(
        identifier,
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrustedParty().orElse(null),
        this.getDocument().orElse(null));
  }

  public Organization withClientCertificate(String clientCertificate) {
    return new Organization(
        this.getIdentifier(),
        clientCertificate,
        this.getIntermediateCertificates(),
        this.getTrustedParty().orElse(null),
        this.getDocument().orElse(null));
  }

  public Organization withIntermediateCertificates(List<String> intermediateCertificates) {
    return new Organization(
        this.getIdentifier(),
        this.getClientCertificate(),
        intermediateCertificates,
        this.getTrustedParty().orElse(null),
        this.getDocument().orElse(null));
  }

  public Organization withTrustedParty(TrustedParty trustedParty) {
    return new Organization(
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        trustedParty,
        this.getDocument().orElse(null));
  }

  public Organization withDocument(Document document) {
    return new Organization(
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrustedParty().orElse(null),
        document);
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

  public Optional<TrustedParty> getTrustedParty() {
    return trustedParty;
  }

  public Optional<Document> getDocument() {
    return document;
  }

  public Either<ApplicationException, Optional<String>> getTrustedPartyBaseUrl() {
    return this.getTrustedParty()
        .map(Either::<ApplicationException, TrustedParty> right)
        .orElse(Either.<ApplicationException, TrustedParty> left(
            new InvalidValueException("TrustedParty for organization with identifier '" + getIdentifier() + "' is not yet hydrated into the model.!")))
        .map(TrustedParty::getUrlIfNotDefault);
  }
}

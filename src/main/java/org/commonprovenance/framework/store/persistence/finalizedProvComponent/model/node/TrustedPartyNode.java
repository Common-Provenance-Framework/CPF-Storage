package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Optional;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasClientCertificate;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasId;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsChecked;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsDefault;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsValid;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasName;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasUrl;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("TrustedParty")
public class TrustedPartyNode implements
    HasId,
    HasName,
    HasClientCertificate,
    HasUrl,
    HasIsChecked,
    HasIsValid,
    HasIsDefault {
  @Id
  @GeneratedValue
  private final String id;
  private final String name;

  @Property("client_certificate")
  private final String clientCertificate;

  private final String url;
  @Property("is_checked")
  private final Boolean isChecked;

  @Property("is_valid")
  private final Boolean isValid;

  @Property("is_default")
  private final Boolean isDefault;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public TrustedPartyNode(
      String id,
      String name,
      String clientCertificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this.id = id;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.url = url;
    this.isChecked = isChecked;
    this.isValid = isValid;
    this.isDefault = isDefault;
  }

  // Constructor for creating new node (id will be generated)
  public TrustedPartyNode(
      String name,
      String clientCertificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this.id = null;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.url = url;
    this.isChecked = isChecked;
    this.isValid = isValid;
    this.isDefault = isDefault;
  }

  public TrustedPartyNode(
      String name,
      String clientCertificate,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this.id = null;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.url = null;
    this.isChecked = isChecked;
    this.isValid = isValid;
    this.isDefault = isDefault;
  }

  public TrustedPartyNode withId(String id) {
    return new TrustedPartyNode(
        id,
        this.getName(),
        this.getClientCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedPartyNode withName(String name) {
    return new TrustedPartyNode(
        this.getId(),
        name,
        this.getClientCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedPartyNode withClientCertificate(String clientCertificate) {
    return new TrustedPartyNode(
        this.getId(),
        this.getName(),
        clientCertificate,
        this.getUrl(),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedPartyNode withUrl(String url) {
    return new TrustedPartyNode(
        this.getId(),
        this.getName(),
        this.getClientCertificate(),
        url,
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedPartyNode withUrl(Optional<String> maybeUrl) {
    return new TrustedPartyNode(
        this.getId(),
        this.getName(),
        this.getClientCertificate(),
        maybeUrl.orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedPartyNode withIsChecked(Boolean isChecked) {
    return new TrustedPartyNode(
        this.getId(),
        this.getName(),
        this.getClientCertificate(),
        this.getUrl(),
        isChecked,
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedPartyNode withIsValid(Boolean isValid) {
    return new TrustedPartyNode(
        this.getId(),
        this.getName(),
        this.getClientCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        isValid,
        this.getIsDefault());
  }

  public TrustedPartyNode withIsDefault(Boolean isDefault) {
    return new TrustedPartyNode(
        this.getId(),
        this.getName(),
        this.getClientCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        this.getIsValid(),
        isDefault);
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getClientCertificate() {
    return clientCertificate;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public Boolean getIsChecked() {
    return isChecked;
  }

  @Override
  public Boolean getIsValid() {
    return isValid;
  }

  @Override
  public Boolean getIsDefault() {
    return isDefault;
  }

}

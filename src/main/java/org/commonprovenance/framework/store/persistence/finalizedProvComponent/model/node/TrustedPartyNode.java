package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("TrustedParty")
public class TrustedPartyNode implements HasId {
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
    this.isValid = isChecked;
    this.isChecked = isValid;
    this.isDefault = isDefault;
  }

  public TrustedPartyNode withId(String id) {
    return new TrustedPartyNode(
        id,
        this.getName(),
        this.getClientCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        this.getIsValid(), this.getIsDefault());
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public String getUrl() {
    return url;
  }

  public Boolean getIsChecked() {
    return isChecked;
  }

  public Boolean getIsValid() {
    return isValid;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

}

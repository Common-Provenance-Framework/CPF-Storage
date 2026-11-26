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
public record TrustedPartyNode(
    @Id @GeneratedValue String id,
    String name,
    @Property("client_certificate") String clientCertificate,
    String url,
    @Property("is_checked") Boolean isChecked,
    @Property("is_valid") Boolean isValid,
    @Property("is_default") Boolean isDefault) implements
    HasId,
    HasName,
    HasClientCertificate,
    HasUrl,
    HasIsChecked,
    HasIsValid,
    HasIsDefault {
  @PersistenceCreator
  public TrustedPartyNode {
  }

  public TrustedPartyNode(
      String name,
      String clientCertificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this(null, name, clientCertificate, url, isChecked, isValid, isDefault);
  }

  public TrustedPartyNode(
      String name,
      String clientCertificate,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this(null, name, clientCertificate, null, isChecked, isValid, isDefault);
  }

  public TrustedPartyNode withName(String name) {
    return new TrustedPartyNode(
        this.id(),
        name,
        this.clientCertificate(),
        this.url(),
        this.isChecked(),
        this.isValid(),
        this.isDefault());
  }

  public TrustedPartyNode withClientCertificate(String clientCertificate) {
    return new TrustedPartyNode(
        this.id(),
        this.name(),
        clientCertificate,
        this.url(),
        this.isChecked(),
        this.isValid(),
        this.isDefault());
  }

  public TrustedPartyNode withUrl(String url) {
    return new TrustedPartyNode(
        this.id(),
        this.name(),
        this.clientCertificate(),
        url,
        this.isChecked(),
        this.isValid(),
        this.isDefault());
  }

  public TrustedPartyNode withUrl(Optional<String> maybeUrl) {
    return new TrustedPartyNode(
        this.id(),
        this.name(),
        this.clientCertificate(),
        maybeUrl.orElse(null),
        this.isChecked(),
        this.isValid(),
        this.isDefault());
  }

  public TrustedPartyNode withIsChecked(Boolean isChecked) {
    return new TrustedPartyNode(
        this.id(),
        this.name(),
        this.clientCertificate(),
        this.url(),
        isChecked,
        this.isValid(),
        this.isDefault());
  }

  public TrustedPartyNode withIsValid(Boolean isValid) {
    return new TrustedPartyNode(
        this.id(),
        this.name(),
        this.clientCertificate(),
        this.url(),
        this.isChecked(),
        isValid,
        this.isDefault());
  }

  public TrustedPartyNode withIsDefault(Boolean isDefault) {
    return new TrustedPartyNode(
        this.id(),
        this.name(),
        this.clientCertificate(),
        this.url(),
        this.isChecked(),
        this.isValid(),
        isDefault);
  }

  @Override
  public String toString() {
    return "TrustedPartyNode [id=" + id
        + ", name=" + name
        + ", clientCertificate=" + clientCertificate
        + ", url=" + url
        + ", isChecked=" + isChecked
        + ", isValid=" + isValid
        + ", isDefault=" + isDefault + "]";
  }

}

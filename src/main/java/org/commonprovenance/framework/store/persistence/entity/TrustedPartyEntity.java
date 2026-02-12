package org.commonprovenance.framework.store.persistence.entity;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("TrustedParty")
public class TrustedPartyEntity implements HasId {
  @Id
  private final String id;
  private final String name;
  private final String clientCertificate;
  private final String url;
  private final Boolean checked;
  private final Boolean valid;

  private final Boolean isDefault;

  public TrustedPartyEntity(
      String id,
      String name,
      String clientCertificate,
      String url,
      Boolean checked,
      Boolean valid,
      Boolean isDefault) {
    this.id = id;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.url = url;
    this.checked = checked;
    this.valid = valid;
    this.isDefault = isDefault;
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

  public Boolean getChecked() {
    return checked;
  }

  public Boolean getValid() {
    return valid;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

}

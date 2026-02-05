package org.commonprovenance.framework.store.persistence.entity;

import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Organization")
public class OrganizationEntity implements HasId {
  @Id
  private final String id;
  private final String name;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  public OrganizationEntity(
      String id,
      String name,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.id = id;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
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

  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

}

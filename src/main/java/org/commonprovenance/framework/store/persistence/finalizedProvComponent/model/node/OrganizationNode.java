package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.Owns;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.Trusts;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Organization")
public class OrganizationNode implements HasIdentifier<OrganizationNode> {
  @Id
  @GeneratedValue
  private final String id;

  private final String identifier;

  @Property("client_certificate")
  private final String clientCertificate;

  @Property("intermediate_certificates")
  private final List<String> intermediateCertificates;

  @Relationship(type = "trusts", direction = Relationship.Direction.OUTGOING)
  private final List<Trusts> trusts;

  @Relationship(type = "owns", direction = Relationship.Direction.OUTGOING)
  private final List<Owns> owns;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public OrganizationNode(
      String id,
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates,
      List<Trusts> trusts,
      List<Owns> owns) {
    this.id = id;
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;

    this.trusts = trusts;
    this.owns = owns;
  }

  // Constructor for creating new node (id will be generated)
  public OrganizationNode(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.id = null;
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;

    this.trusts = Collections.emptyList();
    this.owns = Collections.emptyList();
  }

  // Factory methods
  public OrganizationNode withId(String id) {
    return new OrganizationNode(
        id,
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrusts(),
        this.getOwns());

  }

  public OrganizationNode withIdentifier(String identifier) {
    return new OrganizationNode(
        this.getId(),
        identifier,
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrusts(),
        this.getOwns());

  }

  // Wither method for Neo4j to set relationships
  public OrganizationNode withTrusts(List<Trusts> trusts) {
    return new OrganizationNode(
        this.getId(),
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        trusts,
        this.getOwns());
  }

  public OrganizationNode withOwns(List<Owns> owns) {
    return new OrganizationNode(
        this.getId(),
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrusts(),
        owns);
  }

  public OrganizationNode withTrustedParty(TrustedPartyNode trustedPartyEntity) {
    if (trustedPartyEntity == null) {
      return this;
    }

    List<Trusts> updatedTrusts = Stream.concat(
        this.getTrusts().stream(),
        Stream.of(new Trusts(trustedPartyEntity)))
        .collect(Collectors.toList());

    return new OrganizationNode(
        this.getId(),
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        updatedTrusts,
        this.getOwns());
  }

  public OrganizationNode withDocument(DocumentNode documentNode) {
    if (documentNode == null) {
      return this;
    }

    List<Owns> updatedOwns = Stream.concat(
        this.getOwns().stream(),
        Stream.of(new Owns(documentNode)))
        .collect(Collectors.toList());

    return new OrganizationNode(
        this.getId(),
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrusts(),
        updatedOwns);
  }

  public String getId() {
    return this.id;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

  public List<Trusts> getTrusts() {
    return trusts;
  }

  public List<Owns> getOwns() {
    return owns;
  }

}

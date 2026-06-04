package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.Owns;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.Trusts;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasClientCertificate;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasDocumentNodes;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasId;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIdentifier;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIntermediateCertificates;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasTrustedPartyNodes;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Organization")
public class OrganizationNode implements
    HasId,
    HasIdentifier,
    HasClientCertificate,
    HasIntermediateCertificates,
    HasTrustedPartyNodes,
    HasDocumentNodes {
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

  public OrganizationNode withClientCertificate(String clientCertificate) {
    return new OrganizationNode(
        this.getId(),
        this.getIdentifier(),
        clientCertificate,
        this.getIntermediateCertificates(),
        this.getTrusts(),
        this.getOwns());
  }

  public OrganizationNode withIntermediateCertificates(List<String> intermediateCertificates) {
    return new OrganizationNode(
        this.getId(),
        this.getIdentifier(),
        this.getClientCertificate(),
        intermediateCertificates,
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

  public OrganizationNode withTrustedParty(Optional<TrustedPartyNode> maybeTrustedPartyNode) {
    return maybeTrustedPartyNode.map(this::withTrustedParty).orElse(this);
  }

  public OrganizationNode withTrustedParty(TrustedPartyNode trustedPartyNode) {
    if (trustedPartyNode == null) {
      return this;
    }

    List<Trusts> updatedTrusts = Stream.concat(
        this.getTrusts().stream(),
        Stream.of(new Trusts(trustedPartyNode)))
        .collect(Collectors.toList());

    return new OrganizationNode(
        this.getId(),
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        updatedTrusts,
        this.getOwns());
  }

  public OrganizationNode withDocument(Optional<DocumentNode> maybeDocumentNode) {
    return maybeDocumentNode.map(this::withDocument).orElse(this);
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

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  @Override
  public String getClientCertificate() {
    return clientCertificate;
  }

  @Override
  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

  public List<Trusts> getTrusts() {
    return trusts;
  }

  public List<Owns> getOwns() {
    return owns;
  }

  @Override
  public List<TrustedPartyNode> getTrustedParties() {
    return this.getTrusts().stream()
        .map(Trusts::getTrustedParty)
        .collect(Collectors.toList());
  }

  @Override
  public List<DocumentNode> getDocuments() {
    return this.getOwns().stream()
        .map(Owns::getDocument)
        .collect(Collectors.toList());
  }

}

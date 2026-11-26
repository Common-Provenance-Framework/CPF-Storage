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
public record OrganizationNode(
    @Id @GeneratedValue String id,
    String identifier,
    @Property("client_certificate") String clientCertificate,
    @Property("intermediate_certificates") List<String> intermediateCertificates,
    @Relationship(type = "trusts", direction = Relationship.Direction.OUTGOING) List<Trusts> trusts,
    @Relationship(type = "owns", direction = Relationship.Direction.OUTGOING) List<Owns> owns) implements
    HasId,
    HasIdentifier,
    HasClientCertificate,
    HasIntermediateCertificates,
    HasTrustedPartyNodes,
    HasDocumentNodes {
  @PersistenceCreator
  public OrganizationNode {
  }

  public OrganizationNode(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this(null, identifier, clientCertificate, intermediateCertificates, Collections.emptyList(), Collections.emptyList());
  }

  // Factory methods
  public OrganizationNode withId(String id) {
    return new OrganizationNode(
        id,
        this.identifier(),
        this.clientCertificate(),
        this.intermediateCertificates(),
        this.trusts(),
        this.owns());

  }

  public OrganizationNode withIdentifier(String identifier) {
    return new OrganizationNode(
        this.id(),
        identifier,
        this.clientCertificate(),
        this.intermediateCertificates(),
        this.trusts(),
        this.owns());
  }

  public OrganizationNode withClientCertificate(String clientCertificate) {
    return new OrganizationNode(
        this.id(),
        this.identifier(),
        clientCertificate,
        this.intermediateCertificates(),
        this.trusts(),
        this.owns());
  }

  public OrganizationNode withIntermediateCertificates(List<String> intermediateCertificates) {
    return new OrganizationNode(
        this.id(),
        this.identifier(),
        this.clientCertificate(),
        intermediateCertificates,
        this.trusts(),
        this.owns());
  }

  // Wither method for Neo4j to set relationships
  public OrganizationNode withTrusts(List<Trusts> trusts) {
    return new OrganizationNode(
        this.id(),
        this.identifier(),
        this.clientCertificate(),
        this.intermediateCertificates(),
        trusts,
        this.owns());
  }

  public OrganizationNode withOwns(List<Owns> owns) {
    return new OrganizationNode(
        this.id(),
        this.identifier(),
        this.clientCertificate(),
        this.intermediateCertificates(),
        this.trusts(),
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
        this.trusts().stream(),
        Stream.of(new Trusts(trustedPartyNode)))
        .collect(Collectors.toList());

    return new OrganizationNode(
        this.id(),
        this.identifier(),
        this.clientCertificate(),
        this.intermediateCertificates(),
        updatedTrusts,
        this.owns());
  }

  public OrganizationNode withDocument(Optional<DocumentNode> maybeDocumentNode) {
    return maybeDocumentNode.map(this::withDocument).orElse(this);
  }

  public OrganizationNode withDocument(DocumentNode documentNode) {
    if (documentNode == null) {
      return this;
    }

    List<Owns> updatedOwns = Stream.concat(
        this.owns().stream(),
        Stream.of(new Owns(documentNode)))
        .collect(Collectors.toList());

    return new OrganizationNode(
        this.id(),
        this.identifier(),
        this.clientCertificate(),
        this.intermediateCertificates(),
        this.trusts(),
        updatedOwns);
  }

  @Override
  public List<TrustedPartyNode> trustedParties() {
    return this.trusts().stream()
        .map(Trusts::trustedParty)
        .collect(Collectors.toList());
  }

  @Override
  public List<DocumentNode> documents() {
    return this.owns().stream()
        .map(Owns::document)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "OrganizationNode [id=" + this.id
        + ", identifier=" + this.identifier
        + ", clientCertificate=" + this.clientCertificate
        + ", intermediateCertificates=" + this.intermediateCertificates
        + ", trusts=" + this.trusts
        + ", owns=" + this.owns + "]";
  }

}

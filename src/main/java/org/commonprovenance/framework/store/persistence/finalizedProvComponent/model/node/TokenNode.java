package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.BelongsTo;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.WasIssuedBy;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Token")
public class TokenNode implements HasId {

  @Id
  @GeneratedValue
  private final String id;

  private final String hash;
  private final String signature;

  @Property("organization_identifier")
  private final String organizationIdentifier;

  private final String bundle;

  @Property("hash_function")
  private final String hashFunction;

  @Property("trusted_party_uri")
  private final String trustedPartyUri;

  @Property("trusted_party_certificate")
  private final String trustedPartyCertificate;

  @Property("message_timestamp")
  private final Long messageTimestamp;

  @Property("token_timestamp")
  private final Long tokenTimestamp;

  @Relationship(type = "was_issued_by", direction = Relationship.Direction.OUTGOING)
  private final List<WasIssuedBy> wasIssuedBy;

  @Relationship(type = "belongs_to", direction = Relationship.Direction.OUTGOING)
  private final List<BelongsTo> belongsTo;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public TokenNode(
      String id,
      String hash,
      String signature,
      String organizationIdentifier,
      String bundle,
      String hashFunction,
      String trustedPartyUri,
      String trustedPartyCertificate,
      Long messageTimestamp,
      Long tokenTimestamp,
      List<WasIssuedBy> wasIssuedBy,
      List<BelongsTo> belongsTo) {
    this.id = id;
    this.hash = hash;
    this.signature = signature;
    this.organizationIdentifier = organizationIdentifier;
    this.bundle = bundle;
    this.hashFunction = hashFunction;
    this.trustedPartyUri = trustedPartyUri;
    this.trustedPartyCertificate = trustedPartyCertificate;
    this.messageTimestamp = messageTimestamp;
    this.tokenTimestamp = tokenTimestamp;

    this.wasIssuedBy = wasIssuedBy;
    this.belongsTo = belongsTo;
  }

  // Constructor for creating new node (id will be generated)
  public TokenNode(
      String hash,
      String signature,
      String organizationIdentifier,
      String bundle,
      String hashFunction,
      String trustedPartyUri,
      String trustedPartyCertificate,
      Long messageTimestamp,
      Long tokenTimestamp) {
    this.id = null;
    this.hash = hash;
    this.signature = signature;
    this.organizationIdentifier = organizationIdentifier;
    this.bundle = bundle;
    this.hashFunction = hashFunction;
    this.trustedPartyUri = trustedPartyUri;
    this.trustedPartyCertificate = trustedPartyCertificate;
    this.messageTimestamp = messageTimestamp;
    this.tokenTimestamp = tokenTimestamp;

    this.wasIssuedBy = Collections.emptyList();
    this.belongsTo = Collections.emptyList();
  }

  // Factory methods
  public TokenNode withTrustedParty(TrustedPartyNode trustedPartyEntity) {
    if (trustedPartyEntity == null) {
      return this;
    }

    List<WasIssuedBy> updatedWasIssuedBy = Stream.concat(
        this.getWasIssuedBy().stream(),
        Stream.of(new WasIssuedBy(trustedPartyEntity)))
        .collect(Collectors.toList());

    return new TokenNode(
        this.getId(),
        this.getHash(),
        this.getSignature(),
        this.getOrganizationIdentifier(),
        this.getBundle(),
        this.getHashFunction(),
        this.getTrustedPartyUri(),
        this.getTrustedPartyCertificate(),
        this.getMessageTimestamp(),
        this.getTokenTimestamp(),
        updatedWasIssuedBy,
        this.getBelongsTo());
  }

  // Wither method for Neo4j to set relationships
  public TokenNode withWasIssuedBy(List<WasIssuedBy> wasIssuedBy) {
    return new TokenNode(
        this.getId(),
        this.getHash(),
        this.getSignature(),
        this.getOrganizationIdentifier(),
        this.getBundle(),
        this.getHashFunction(),
        this.getTrustedPartyUri(),
        this.getTrustedPartyCertificate(),
        this.getMessageTimestamp(),
        this.getTokenTimestamp(),
        wasIssuedBy,
        this.getBelongsTo());
  }

  public TokenNode withDocument(DocumentNode documentEntity) {
    if (documentEntity == null) {
      return this;
    }

    List<BelongsTo> updatedBelongsTo = Stream.concat(
        this.getBelongsTo().stream(),
        Stream.of(new BelongsTo(documentEntity)))
        .collect(Collectors.toList());

    return new TokenNode(
        this.getId(),
        this.getHash(),
        this.getSignature(),
        this.getOrganizationIdentifier(),
        this.getBundle(),
        this.getHashFunction(),
        this.getTrustedPartyUri(),
        this.getTrustedPartyCertificate(),
        this.getMessageTimestamp(),
        this.getTokenTimestamp(),
        this.getWasIssuedBy(),
        updatedBelongsTo);
  }

  // Wither method for Neo4j to set relationships
  public TokenNode withBelongsTo(List<BelongsTo> belongsTo) {
    return new TokenNode(
        this.getId(),
        this.getHash(),
        this.getSignature(),
        this.getOrganizationIdentifier(),
        this.getBundle(),
        this.getHashFunction(),
        this.getTrustedPartyUri(),
        this.getTrustedPartyCertificate(),
        this.getMessageTimestamp(),
        this.getTokenTimestamp(),
        this.getWasIssuedBy(),
        belongsTo);
  }

  public String getId() {
    return this.id;
  }

  public String getHash() {
    return hash;
  }

  public String getSignature() {
    return signature;
  }

  public String getOrganizationIdentifier() {
    return organizationIdentifier;
  }

  public String getBundle() {
    return bundle;
  }

  public String getHashFunction() {
    return hashFunction;
  }

  public String getTrustedPartyUri() {
    return trustedPartyUri;
  }

  public String getTrustedPartyCertificate() {
    return trustedPartyCertificate;
  }

  public Long getMessageTimestamp() {
    return messageTimestamp;
  }

  public Long getTokenTimestamp() {
    return tokenTimestamp;
  }

  public List<WasIssuedBy> getWasIssuedBy() {
    return wasIssuedBy;
  }

  public List<BelongsTo> getBelongsTo() {
    return belongsTo;
  }

}

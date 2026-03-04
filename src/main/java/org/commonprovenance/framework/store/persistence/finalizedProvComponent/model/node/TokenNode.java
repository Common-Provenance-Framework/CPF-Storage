package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.BelongsTo;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.WasIssuedBy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Token")
public class TokenNode implements HasId {

  @Id
  private final @NonNull String id;

  private final String hash;
  private final String signature;

  @Property("originator_id")
  private final String originatorId;

  @Property("additional_data")
  private final String additionalData;

  @Property("message_timestamp")
  private final Long messageTimestamp;

  @Property("token_timestamp")
  private final Long tokenTimestamp;

  @Relationship(type = "was_issued_by", direction = Relationship.Direction.OUTGOING)
  private final List<WasIssuedBy> wasIssuedBy;

  @Relationship(type = "belongs_to", direction = Relationship.Direction.OUTGOING)
  private final List<BelongsTo> belongsTo;

  @PersistenceCreator
  public TokenNode(
      String id,
      String hash,
      String signature,
      String originatorId,
      String additionalData,
      Long messageTimestamp,
      Long tokenTimestamp,
      List<WasIssuedBy> wasIssuedBy,
      List<BelongsTo> belongsTo) {
    this.id = id;
    this.hash = hash;
    this.signature = signature;
    this.originatorId = originatorId;
    this.additionalData = additionalData;
    this.messageTimestamp = messageTimestamp;
    this.tokenTimestamp = tokenTimestamp;

    this.wasIssuedBy = wasIssuedBy;
    this.belongsTo = belongsTo;
  }

  public TokenNode(
      String id,
      String hash,
      String signature,
      String originatorId,
      String additionalData,
      Long messageTimestamp,
      Long tokenTimestamp) {
    this.id = id;
    this.hash = hash;
    this.signature = signature;
    this.originatorId = originatorId;
    this.additionalData = additionalData;
    this.messageTimestamp = messageTimestamp;
    this.tokenTimestamp = tokenTimestamp;

    this.wasIssuedBy = Collections.emptyList();
    this.belongsTo = Collections.emptyList();
  }

  // Factory methods
  public @NonNull TokenNode withTrustedParty(@Nullable TrustedPartyNode trustedPartyEntity) {
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
        this.getOriginatorId(),
        this.getAdditionalData(),
        this.getMessageTimestamp(),
        this.getTokenTimestamp(),
        updatedWasIssuedBy,
        this.getBelongsTo());
  }

  // Wither method for Neo4j to set relationships
  public @NonNull TokenNode withWasIssuedBy(@NonNull List<WasIssuedBy> wasIssuedBy) {
    return new TokenNode(
        this.getId(),
        this.getHash(),
        this.getSignature(),
        this.getOriginatorId(),
        this.getAdditionalData(),
        this.getMessageTimestamp(),
        this.getTokenTimestamp(),
        wasIssuedBy,
        this.getBelongsTo());
  }

  // Factory methods
  public @NonNull TokenNode withDocument(@Nullable DocumentNode documentEntity) {
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
        this.getOriginatorId(),
        this.getAdditionalData(),
        this.getMessageTimestamp(),
        this.getTokenTimestamp(),
        this.getWasIssuedBy(),
        updatedBelongsTo);
  }

  public @NonNull TokenNode withBelongsTo(@NonNull List<BelongsTo> belongsTo) {
    return new TokenNode(
        this.getId(),
        this.getHash(),
        this.getSignature(),
        this.getOriginatorId(),
        this.getAdditionalData(),
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

  public String getOriginatorId() {
    return originatorId;
  }

  public String getAdditionalData() {
    return additionalData;
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

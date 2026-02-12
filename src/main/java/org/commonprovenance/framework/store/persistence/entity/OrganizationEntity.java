package org.commonprovenance.framework.store.persistence.entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.relation.Trusts;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Organization")
public class OrganizationEntity implements HasId {
  @Id
  private final String id;
  private final String name;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  @Relationship(type = "trusts", direction = Relationship.Direction.OUTGOING)
  private final List<Trusts> trusts;

  public OrganizationEntity(
      String id,
      String name,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.id = id;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;

    this.trusts = Collections.emptyList();
  }

  public OrganizationEntity(
      String id,
      String name,
      String clientCertificate,
      List<String> intermediateCertificates,
      List<Trusts> trusts) {
    this.id = id;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;

    this.trusts = trusts;
  }

  // Factory methods
  public @NonNull OrganizationEntity withTrusts(@Nullable TrustedParty trustedParty) {
    Function<TrustedParty, List<Trusts>> addTrustedParty = tp -> Stream.concat(
        this.getTrusts().stream(),
        Stream.of(new Trusts(tp)))
        .collect(Collectors.toList());

    return new OrganizationEntity(
        this.getId(),
        this.getName(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),

        Optional.ofNullable(trustedParty)
            .map(addTrustedParty)
            .orElse(this.getTrusts()));
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

  public List<Trusts> getTrusts() {
    return trusts;
  }

}

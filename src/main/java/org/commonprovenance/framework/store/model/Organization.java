package org.commonprovenance.framework.store.model;

import java.util.UUID;

import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;

public class Organization {
  private final UUID id;
  private final String name;

  public Organization(UUID id, String name) {
    this.id = id;
    this.name = name;
  }

  public static Organization fromDto(OrganizationResponseDTO dto) {
    return new Organization(null, dto.getName());
  }

  public Organization withId(UUID id) {
    return new Organization(id, this.getName());
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}

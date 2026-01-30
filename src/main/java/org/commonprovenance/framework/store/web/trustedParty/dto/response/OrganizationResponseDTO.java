package org.commonprovenance.framework.store.web.trustedParty.dto.response;

public class OrganizationResponseDTO {
  private final String id;
  private final String name;

  public OrganizationResponseDTO(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}

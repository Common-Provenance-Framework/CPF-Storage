package org.commonprovenance.framework.store.web.trustedParty.dto.form;

public class OrganizationFormDTO {
  private final String name;

  public OrganizationFormDTO(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

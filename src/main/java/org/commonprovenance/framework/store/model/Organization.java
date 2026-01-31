package org.commonprovenance.framework.store.model;

import java.util.UUID;

public class Organization {
  private final UUID id;
  private final String name;

  public Organization(UUID id, String name) {
    this.id = id;
    this.name = name;
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

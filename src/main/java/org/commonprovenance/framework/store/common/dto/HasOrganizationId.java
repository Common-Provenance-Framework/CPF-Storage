package org.commonprovenance.framework.store.common.dto;

public interface HasOrganizationId<T extends HasOrganizationId<T>> {

  String getOrganizationId();

  T withOrganizationId(String organizationId);
}

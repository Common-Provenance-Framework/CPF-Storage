package org.commonprovenance.framework.store.common.dto;

public interface HasCreatedOn<T extends HasCreatedOn<T>> {
  Long getCreatedOn();

  T withCreatedOn(Long createdOn);
}

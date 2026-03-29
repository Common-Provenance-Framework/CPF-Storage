package org.commonprovenance.framework.store.common.dto;

public interface HasIdentifier<T extends HasIdentifier<T>> {
  String getIdentifier();

  T withIdentifier(String identifier);

}

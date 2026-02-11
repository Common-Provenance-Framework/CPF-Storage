package org.commonprovenance.framework.store.common.dto;

public interface HasDocument<T extends HasDocument<T>> {
  String getDocument();

  T withDocument(String graph);
}

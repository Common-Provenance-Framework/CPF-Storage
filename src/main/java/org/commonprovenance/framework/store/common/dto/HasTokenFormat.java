package org.commonprovenance.framework.store.common.dto;

import java.util.function.UnaryOperator;

public interface HasTokenFormat<T extends HasTokenFormat<T>> {
  String getTokenFormat();

  T withTokenFormat(String tokenFormat);

  static <T extends HasTokenFormat<T>> UnaryOperator<T> setJwtFormat() {
    return (T to) -> to.withTokenFormat("jwt");
  }

}

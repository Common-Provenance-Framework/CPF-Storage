package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;

public interface HasCpmDocument<T extends HasCpmDocument<T>> {
  Optional<String> getIdentifier();

}

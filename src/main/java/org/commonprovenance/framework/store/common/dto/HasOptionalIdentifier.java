package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;

public interface HasOptionalIdentifier {
  Optional<String> getIdentifier();
}

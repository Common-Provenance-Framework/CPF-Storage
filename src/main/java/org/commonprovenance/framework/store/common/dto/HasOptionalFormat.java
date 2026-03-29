package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Format;

public interface HasOptionalFormat {
  Optional<Format> getFormat();
}

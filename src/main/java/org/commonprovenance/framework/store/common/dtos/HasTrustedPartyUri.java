package org.commonprovenance.framework.store.common.dtos;

import java.util.Optional;

public interface HasTrustedPartyUri {
  String trustedPartyUri();

  Optional<String> maybeTrustedPartyUri();
}

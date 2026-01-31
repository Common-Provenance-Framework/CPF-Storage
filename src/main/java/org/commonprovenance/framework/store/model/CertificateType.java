package org.commonprovenance.framework.store.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum CertificateType {
  ROOT("root"),
  INTERMEDIATE("intermediate"),
  CLIENT("client");

  private final Set<String> aliases;
  private static final Map<String, CertificateType> LOOKUP;

  static {
    LOOKUP = Arrays.stream(values())
        .flatMap(f -> f.aliases.stream().map(a -> Map.entry(a.toLowerCase(), f)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  CertificateType(String... aliases) {
    this.aliases = Set.of(aliases);
  }

  public static Optional<CertificateType> from(String s) {
    if (s == null)
      return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(s.trim().toLowerCase()));
  }
}

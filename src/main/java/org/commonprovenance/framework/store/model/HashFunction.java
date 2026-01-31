package org.commonprovenance.framework.store.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum HashFunction {
  SHA256("SHA256"),
  SHA512("SHA512"),
  SHA3_256("SHA3-256"),
  SHA3_512("SHA3-512");

  private final Set<String> aliases;
  private static final Map<String, HashFunction> LOOKUP;

  static {
    LOOKUP = Arrays.stream(values())
        .flatMap(f -> f.aliases.stream().map(a -> Map.entry(a.toLowerCase(), f)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  HashFunction(String... aliases) {
    this.aliases = Set.of(aliases);
  }

  public static Optional<HashFunction> from(String s) {
    if (s == null)
      return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(s.trim().toLowerCase()));
  }
}

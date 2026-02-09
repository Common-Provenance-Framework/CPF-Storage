package org.commonprovenance.framework.store.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum GraphType {
  DOMAIN_SPECIFIC("domain_specific"),
  BACKBONE("backbone"),
  META("meta"),
  GRAPH("graph");

  private final Set<String> aliases;
  private static final Map<String, GraphType> LOOKUP;

  static {
    LOOKUP = Arrays.stream(values())
        .flatMap((GraphType type) -> type.aliases.stream().map((String value) -> Map.entry(value.toLowerCase(), type)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  GraphType(String... aliases) {
    this.aliases = Set.of(aliases);
  }

  public static Optional<GraphType> from(String value) {
    if (value == null)
      return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(value.trim().toLowerCase()));
  }
}

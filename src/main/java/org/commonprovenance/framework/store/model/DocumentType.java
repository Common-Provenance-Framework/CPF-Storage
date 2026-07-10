package org.commonprovenance.framework.store.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum DocumentType {
  DOMAIN_SPECIFIC("domain_specific"),
  BACKBONE("backbone"),
  META("meta"),
  GRAPH("graph");

  private final Set<String> aliases;
  private static final Map<String, DocumentType> LOOKUP;

  static {
    LOOKUP = Arrays.stream(values())
        .flatMap((DocumentType type) -> type.aliases.stream().map((String value) -> Map.entry(value.toLowerCase(), type)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  DocumentType(String... aliases) {
    this.aliases = Set.of(aliases);
  }

  public static Optional<DocumentType> from(String value) {
    if (value == null)
      return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(value.trim().toLowerCase()));
  }
}

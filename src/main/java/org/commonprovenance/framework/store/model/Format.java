package org.commonprovenance.framework.store.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openprovenance.prov.model.interop.Formats;

public enum Format {
  JSON("json");

  private final Set<String> aliases;
  private static final Map<String, Format> LOOKUP;

  static {
    LOOKUP = Arrays.stream(values())
        .flatMap(f -> f.aliases.stream().map(a -> Map.entry(a.toLowerCase(), f)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  Format(String... aliases) {
    this.aliases = Set.of(aliases);
  }

  public static Optional<Format> from(String s) {
    if (s == null)
      return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(s.trim().toLowerCase()));
  }

  public Formats.ProvFormat toProvFormat() {
    switch (this) {
      case JSON:
        return Formats.ProvFormat.JSON;
      default:
        throw new IllegalStateException("Unsupported format: " + this);
    }
  }
}

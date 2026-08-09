package org.commonprovenance.framework.store.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openprovenance.prov.model.interop.Formats;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Format", description = "Supported document format")
public enum GraphFormat {
  @Schema(description = "PROV JSON format")
  JSON("json");

  private final Set<String> aliases;
  private static final Map<String, GraphFormat> LOOKUP;

  static {
    LOOKUP = Arrays.stream(values())
        .flatMap(f -> f.aliases.stream().map(a -> Map.entry(a.toLowerCase(), f)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  GraphFormat(String... aliases) {
    this.aliases = Set.of(aliases);
  }

  public static Optional<GraphFormat> from(String s) {
    if (s == null)
      return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(s.trim().toLowerCase()));
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static GraphFormat fromJson(String value) {
    return from(value)
        .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + value));
  }

  public Formats.ProvFormat toProvFormat() {
    switch (this) {
      case JSON:
        return Formats.ProvFormat.JSON;
      default:
        throw new IllegalStateException("Unsupported format: " + this);
    }
  }

  public Formats.ProvFormat toProvFormat(Formats.ProvFormat defaultFormat) {
    switch (this) {
      case JSON:
        return Formats.ProvFormat.JSON;
      default:
        return defaultFormat;
    }
  }
}

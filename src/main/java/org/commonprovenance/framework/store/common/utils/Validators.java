package org.commonprovenance.framework.store.common.utils;

import java.util.regex.Pattern;

public class Validators {
  private static final Pattern UUID_PATTERN = Pattern.compile(
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  private static final Pattern ISO8601_PATTERN = Pattern.compile(
      "^\\d{4}-" + // Year: 4 digits
          "(0[1-9]|1[0-2])-" + // Month: 01-12
          "(0[1-9]|[12][0-9]|3[01])T" + // Day: 01-31
          "([01][0-9]|2[0-3]):" + // Hour: 00-23
          "([0-5][0-9]):" + // Minute: 00-59
          "([0-5][0-9])" + // Second: 00-59
          "(\\.\\d+)?" + // Optional fractional seconds
          "(Z|[+-]([01][0-9]|2[0-3]):[0-5][0-9])$" // Timezone
  );

  public static Boolean isUUID(String value) {
    // 8dc3f8a5-92a7-4399-a300-3aa164862847
    return value != null
        && value.length() == 36
        && UUID_PATTERN.matcher(value).matches();
  }

  public static boolean isISO8601DateTime(String value) {
    // "2026-01-30T14:30:00+02:00"
    return value != null
        && !ISO8601_PATTERN.matcher(value).matches();
  }
}

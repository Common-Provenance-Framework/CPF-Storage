package org.commonprovenance.framework.store.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized audit logger for security-relevant events.
 *
 * <p>
 * All audit entries are emitted through the named {@code AUDIT} logger so
 * that they can be routed independently (e.g. to {@code logs/audit.log}) via
 * the Logback configuration without mixing with general application logs.
 *
 * <p>
 * Each method uses a fixed event-type token as the first field so audit
 * entries are easy to filter, parse, and ship to a SIEM or log aggregator.
 *
 * <p>
 * Expand this class when Spring Security is added to the project.
 */
@Component
public class AuditLogger {

  private static final Logger audit = LoggerFactory.getLogger("AUDIT");

  /**
   * Log a successful authentication event.
   *
   * @param principal the authenticated identity (username, subject, etc.)
   * @param clientIp  the originating IP address
   */
  public void logAuthSuccess(String principal, String clientIp) {
    audit.info("AUTH_SUCCESS principal={} ip={}", principal, clientIp);
  }

  /**
   * Log a failed authentication attempt.
   *
   * @param reason   short reason string (e.g. "BAD_CREDENTIALS", "TOKEN_EXPIRED")
   * @param clientIp the originating IP address
   */
  public void logAuthFailure(String reason, String clientIp) {
    audit.warn("AUTH_FAILURE reason={} ip={}", reason, clientIp);
  }

  /**
   * Log an authorization failure (authenticated but not permitted).
   *
   * @param principal the authenticated identity
   * @param resource  the resource or endpoint that was denied
   * @param clientIp  the originating IP address
   */
  public void logAccessDenied(String principal, String resource, String clientIp) {
    audit.warn("ACCESS_DENIED principal={} resource={} ip={}", principal, resource, clientIp);
  }

  /**
   * Log a generic audit event that does not fit the typed methods above.
   *
   * @param event   short event-type token in UPPER_SNAKE_CASE
   * @param details free-form detail string (key=value pairs recommended)
   */
  public void log(String event, String details) {
    audit.info("{} {}", event, details);
  }
}

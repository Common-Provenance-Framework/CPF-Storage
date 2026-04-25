package org.commonprovenance.framework.store.filter;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLogFilter implements WebFilter {

  private static final Logger log = LoggerFactory.getLogger("ACCESS");
  private static final String UNKNOWN = "-";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    if (isExcludedPath(path))
      return chain.filter(exchange);

    long startNanos = System.nanoTime();

    return chain.filter(exchange)
        .doFinally(signalType -> {
          long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
          int status = resolveStatus(exchange, signalType);
          String method = exchange.getRequest().getMethod() != null
              ? exchange.getRequest().getMethod().name()
              : UNKNOWN;
          String requestPath = withQuery(exchange);
          String clientIp = resolveClientIp(exchange);
          String userAgent = resolveUserAgent(exchange);
          String requestId = (String) exchange.getAttributes()
              .getOrDefault(CorrelationIdFilter.REQUEST_ID_ATTR, UNKNOWN);

          String message = "{} {} {} {}ms requestId={} ip={} ua=\"{}\"";
          if (status >= 500)
            log.error(message, method, requestPath, status, durationMs, requestId, clientIp, userAgent);
          else if (status >= 400)
            log.warn(message, method, requestPath, status, durationMs, requestId, clientIp, userAgent);
          else
            log.info(message, method, requestPath, status, durationMs, requestId, clientIp, userAgent);
        });
  }

  private boolean isExcludedPath(String path) {
    if (path == null)
      return true;

    return path.equals("/swagger-ui.html")
        || path.startsWith("/swagger-ui/")
        || path.equals("/v3/api-docs")
        || path.startsWith("/v3/api-docs/");
  }

  private String withQuery(ServerWebExchange exchange) {
    String path = exchange.getRequest().getURI().getPath();
    String query = exchange.getRequest().getURI().getRawQuery();
    if (query == null || query.isBlank())
      return path;

    return path + "?" + query;
  }

  private int resolveStatus(ServerWebExchange exchange, SignalType signalType) {
    HttpStatusCode code = exchange.getResponse().getStatusCode();
    if (code != null)
      return code.value();

    if (signalType == SignalType.ON_ERROR)
      return HttpStatus.INTERNAL_SERVER_ERROR.value();

    return HttpStatus.OK.value();
  }

  private String resolveClientIp(ServerWebExchange exchange) {
    String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      String firstIp = xForwardedFor.split(",")[0].trim();
      if (!firstIp.isBlank())
        return firstIp;
    }

    String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
    if (xRealIp != null && !xRealIp.isBlank())
      return xRealIp.trim();

    InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
    if (remoteAddress != null && remoteAddress.getAddress() != null)
      return remoteAddress.getAddress().getHostAddress();

    return UNKNOWN;
  }

  private String resolveUserAgent(ServerWebExchange exchange) {
    String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
    if (userAgent == null || userAgent.isBlank())
      return UNKNOWN;

    return userAgent;
  }
}

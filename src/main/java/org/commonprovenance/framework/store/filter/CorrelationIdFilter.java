package org.commonprovenance.framework.store.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Assigns a unique request ID to every incoming request and propagates it
 * through the Reactor Context so that all downstream loggers include it in
 * their MDC (via {@link MdcContextLifter}).
 *
 * <p>
 * The request ID is sourced from the {@code X-Request-ID} request header
 * when present (so callers can pass in their own correlation ID); otherwise a
 * random UUID is generated. The resolved ID is echoed back in the
 * {@code X-Request-ID} response header.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CorrelationIdFilter implements WebFilter {

  private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

  static final String REQUEST_ID_HEADER = "X-Request-ID";
  static final String REQUEST_ID_ATTR = "requestId";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String requestId = resolveRequestId(exchange);

    exchange.getAttributes().put(REQUEST_ID_ATTR, requestId);
    exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);

    log.debug("Assigned requestId={}", requestId);

    return chain.filter(exchange)
        .contextWrite(ctx -> ctx.put(MdcContextLifter.REQUEST_ID_KEY, requestId));
  }

  private String resolveRequestId(ServerWebExchange exchange) {
    String incoming = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
    if (incoming != null && !incoming.isBlank())
      return incoming.trim();

    return UUID.randomUUID().toString();
  }
}

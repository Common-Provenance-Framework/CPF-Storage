package org.commonprovenance.framework.store.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.commonprovenance.framework.store.filter.MdcContextLifter;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

/**
 * Installs a global Reactor operator hook that copies select Reactor Context
 * values (currently {@code requestId}) into SLF4J MDC on every reactive signal,
 * making MDC-based log patterns like {@code %X{requestId}} work correctly
 * across
 * all reactive threads.
 */
@Configuration
public class ReactorMDCConfig {

  private static final String HOOK_KEY = "MDC";

  @PostConstruct
  public void installHook() {
    Hooks.onEachOperator(HOOK_KEY,
        Operators.<Object, Object>lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber)));
  }

  @PreDestroy
  public void removeHook() {
    Hooks.resetOnEachOperator(HOOK_KEY);
  }
}

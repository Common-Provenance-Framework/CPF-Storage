package org.commonprovenance.framework.store.persistence.config;

import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.service.persistence.FinalizedProvComponentService;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("live & neo4j & webflux")
public class TrustedPartyStartupInitializer {

  private static final Logger log = LoggerFactory.getLogger(TrustedPartyStartupInitializer.class);

  @Order(Ordered.HIGHEST_PRECEDENCE + 10)
  @Bean
  ApplicationRunner ensureDefaultTrustedParty(
      FinalizedProvComponentService finalizedProvComponentService,
      TrustedPartyWeb trustedPartyWeb) {
    return _ -> finalizedProvComponentService.getDefaultTrustedParty()
        .doOnSuccess(trustedParty -> log.info("Default TrustedParty '{}' already exists.", trustedParty.getName()))
        .onErrorResume(
            NotFoundException.class,
            _ -> trustedPartyWeb.getTrustedParty(Optional.empty())
                .delayUntil(finalizedProvComponentService::storeTrustedParty)
                .doOnSuccess(trustedParty -> log.info("Created default TrustedParty '{}' during startup.", trustedParty.getName())))
        .doOnError(throwable -> log.error("Default TrustedParty initialization failed during startup. Details: {}", throwable.getMessage()))
        .block();
  }
}

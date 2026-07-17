package org.commonprovenance.framework.store.support;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestConfiguration {

  @Bean
  @Primary
  TrustedPartyWeb testTrustedPartyWeb() {
    return new TrustedPartyWeb() {

      @Override
      public Mono<TrustedParty> getTrustedParty(Optional<String> optTrustedPartyBaseUrl) {
        return Mono.just(new TrustedParty(
            "default",
            "certificate",
            "http://localhost:8093/api/v1",
            true,
            true,
            true));
      }

      @Override
      public Function<Organization, Mono<Token>> issueGraphToken(String signature) {
        return _ -> Mono.empty();
      }

      @Override
      public Function<Organization, Mono<Token>> issueGraphToken(GraphType graphType) {
        return _ -> Mono.empty();
      }

      @Override
      public Function<Organization, Mono<Boolean>> verifySignature(String signature) {
        return _ -> Mono.just(true);
      }
    };
  }
}

package org.commonprovenance.framework.store.web.trustedParty.client.webFlux.config;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.validation.constraints.NotNull;

@Configuration
public class WebConfig {
  private final Environment env;

  WebConfig(Environment env) {
    this.env = env;
  }

  public String getTrustedPartyUrl() {
    return env.getProperty(
        "spring.trusted-party.url",
        String.class,
        "http://127.0.0.1:8081/api/v1");
  }

  @Bean
  @NotNull
  public WebClient webClient() {
    return Objects.requireNonNull(
        WebClient.builder()
            .baseUrl(this.getTrustedPartyUrl())
            .defaultHeader("Accept", "application/json")
            .build(),
        "Trusted Party client can not be null!");
  }
}

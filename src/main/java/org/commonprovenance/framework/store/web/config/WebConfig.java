package org.commonprovenance.framework.store.web.config;

import java.util.Objects;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.validation.constraints.NotNull;

@Configuration
@Validated
public class WebConfig {
  private final Environment env;

  WebConfig(Environment env) {
    this.env = env;
  }

  public String getTrustedPartyUrl() {
    return env.getProperty(
        "trusted-party.url",
        String.class,
        "http://127.0.0.1:8081/api/v1");
  }

  public String getStoreUrl() {
    return env.getProperty(
        "store.url",
        String.class,
        "http://127.0.0.1:8080/api/v1");
  }

  @NotNull
  public WebClient getDefaultTrustedPartyWebClient() {
    return Objects.requireNonNull(
        WebClient.builder()
            .baseUrl(this.getTrustedPartyUrl())
            .defaultHeader("Accept", "application/json")
            .build(),
        "Trusted Party client can not be null!");
  }

  @NotNull
  public WebClient getDefaultStoreWebClient() {
    return Objects.requireNonNull(
        WebClient.builder()
            .baseUrl(this.getStoreUrl())
            .defaultHeader("Accept", "application/json")
            .build(),
        "Store client can not be null!");
  }

}

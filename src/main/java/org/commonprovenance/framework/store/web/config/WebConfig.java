package org.commonprovenance.framework.store.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {
  public static final String STORE_CLIENT = "storeWebClient";
  public static final String TRUSTED_PARTY_CLIENT = "trustedPartyWebClient";

  private final Environment env;

  WebConfig(Environment env) {
    this.env = env;
  }

  public String getTrustedPartyBaseUrl() {
    return env.getProperty(
        "trusted-party.url",
        String.class,
        "http://127.0.0.1:8081/api/v1");
  }

  public String getStoreBaseUrl() {
    return env.getProperty(
        "store.url",
        String.class,
        "http://127.0.0.1:8080/api/v1");
  }

  // Shared base: every derived client reuses its connector (connection pool).
  @Bean
  @Primary
  WebClient baseWebClient() {
    return WebClient.builder()
        .defaultHeader("Accept", "application/json")
        .build();
  }

  @Bean(STORE_CLIENT)
  WebClient storeWebClient(WebClient baseWebClient) {
    return baseWebClient.mutate().baseUrl(this.getStoreBaseUrl()).build();
  }

  @Bean(TRUSTED_PARTY_CLIENT)
  WebClient trustedPartyWebClient(WebClient baseWebClient) {
    return baseWebClient.mutate().baseUrl(this.getTrustedPartyBaseUrl()).build();
  }
}

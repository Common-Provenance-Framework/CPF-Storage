package org.commonprovenance.framework.store.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the shared base and service-specific {@link WebClient} beans used by the web layer.
 *
 * <p>
 * The storage and trusted-party clients inherit the shared client's configuration and use their respective base URLs from the application environment.
 */
@Configuration
public class WebConfig {
  /** Spring bean name for the storage-service {@link WebClient}. */
  public static final String STORE_CLIENT = "storeWebClient";

  /** Spring bean name for the trusted-party {@link WebClient}. */
  public static final String TRUSTED_PARTY_CLIENT = "trustedPartyWebClient";

  private final Environment env;

  /**
   * Creates the web configuration using the application environment.
   *
   * @param env application environment used to resolve service base URLs
   */
  WebConfig(Environment env) {
    this.env = env;
  }

  /**
   * Returns the trusted-party base URL from {@code trusted-party.url}.
   *
   * @return the configured URL, or {@code http://127.0.0.1:8081/api/v1} if the property is absent
   */
  public String getTrustedPartyBaseUrl() {
    return env.getProperty(
        "trusted-party.url",
        String.class,
        "http://127.0.0.1:8081/api/v1");
  }

  /**
   * Returns the storage-service base URL from {@code store.url}.
   *
   * @return the configured URL, or {@code http://127.0.0.1:8080/api/v1} if the property is absent
   */
  public String getStoreBaseUrl() {
    return env.getProperty(
        "store.url",
        String.class,
        "http://127.0.0.1:8080/api/v1");
  }

  /**
   * Creates the shared base client from which the service-specific clients are derived.
   *
   * @return a base client with the JSON {@code Accept} header configured
   */
  // Shared base: every derived client reuses its connector (connection pool).
  @Bean
  @Primary
  WebClient baseWebClient() {
    return WebClient.builder()
        .defaultHeader("Accept", "application/json")
        .build();
  }

  /**
   * Creates the storage-service client using the shared client's configuration.
   *
   * @param baseWebClient shared base client
   * @return a client configured with the storage-service base URL
   */
  @Bean(STORE_CLIENT)
  WebClient storeWebClient(WebClient baseWebClient) {
    return baseWebClient.mutate().baseUrl(this.getStoreBaseUrl()).build();
  }

  /**
   * Creates the trusted-party client using the shared client's configuration.
   *
   * @param baseWebClient shared base client
   * @return a client configured with the trusted-party base URL
   */
  @Bean(TRUSTED_PARTY_CLIENT)
  WebClient trustedPartyWebClient(WebClient baseWebClient) {
    return baseWebClient.mutate().baseUrl(this.getTrustedPartyBaseUrl()).build();
  }
}

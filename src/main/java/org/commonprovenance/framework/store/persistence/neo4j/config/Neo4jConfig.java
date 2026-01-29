package org.commonprovenance.framework.store.persistence.neo4j.config;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.neo4j.config.AbstractReactiveNeo4jConfig;

import jakarta.validation.constraints.NotNull;

@Configuration
public class Neo4jConfig extends AbstractReactiveNeo4jConfig {
  private final Environment env;

  Neo4jConfig(Environment env) {
    this.env = env;
  }

  @Bean(destroyMethod = "close")
  @NotNull
  public Driver driver() {
    String uri = env.getProperty(
        "spring.neo4j.uri",
        String.class,
        "bolt://127.0.0.1:7687");
    String username = env.getProperty(
        "spring.neo4j.authentication.username",
        String.class,
        "neo4j");
    String password = env.getProperty(
        "spring.neo4j.authentication.password",
        String.class,
        "password");
    Integer timeout = env.getProperty(
        "spring.neo4j.timeout",
        Integer.class,
        30);

    var auth = (username != null && !username.isBlank())
        ? AuthTokens.basic(username, password)
        : AuthTokens.none();

    Config config = Config.builder()
        .withConnectionTimeout(timeout, TimeUnit.SECONDS)
        .build();

    return Objects.requireNonNull(
        GraphDatabase.driver(uri, auth, config),
        "Neo4j Driver can not be null!");
  }
}

package org.commonprovenance.framework.store.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.neo4j.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

public class Neo4jContainerInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final String NEO4J_IMAGE = "neo4j:2026.02.2-trixie";
  private static final String NEO4J_USERNAME = "neo4j";
  private static final String NEO4J_PASSWORD = "testpassword";

  private static final Neo4jContainer NEO4J = new Neo4jContainer(DockerImageName.parse(NEO4J_IMAGE))
      .withAdminPassword(NEO4J_PASSWORD)
      .withReuse(true);

  static {
    NEO4J.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(
        "spring.neo4j.uri=" + NEO4J.getBoltUrl(),
        "spring.neo4j.authentication.username=" + NEO4J_USERNAME,
        "spring.neo4j.authentication.password=" + NEO4J.getAdminPassword())
        .applyTo(applicationContext.getEnvironment());
  }
}

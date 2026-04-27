package org.commonprovenance.framework.store.persistence.config;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("live & neo4j")
public class Neo4jSchemaInitializer {

  private static final Logger log = LoggerFactory.getLogger(Neo4jSchemaInitializer.class);

  private static final int DUPLICATE_SAMPLE_LIMIT = 20;

  private static final List<TrustedPartyConstraintSpec> TRUSTED_PARTY_CONSTRAINTS = List.of(
      new TrustedPartyConstraintSpec("trusted_party_name_unique", "trusted_party_name_required", "name"),
      new TrustedPartyConstraintSpec("trusted_party_url_unique", "trusted_party_url_required", "url"));

  private static final List<IdentifierConstraintSpec> IDENTIFIER_CONSTRAINTS = List.of(
      new IdentifierConstraintSpec("Bundle", "bundle_identifier_unique", "bundle_identifier_required"),
      new IdentifierConstraintSpec("Entity", "entity_identifier_unique", "entity_identifier_required"),
      new IdentifierConstraintSpec("Activity", "activity_identifier_unique", "activity_identifier_required"),
      new IdentifierConstraintSpec("Agent", "agent_identifier_unique", "agent_identifier_required"),
      new IdentifierConstraintSpec("Document", "document_identifier_unique", "document_identifier_required"),
      new IdentifierConstraintSpec("Organization", "organization_identifier_unique", "organization_identifier_required"));

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  ApplicationRunner initializeNeo4jSchema(Driver driver) {
    return _ -> {
      try (var session = driver.session()) {
        IDENTIFIER_CONSTRAINTS.forEach(spec -> {
          ensureNoDuplicateIdentifiers(session, spec);
          ensureNoMissingIdentifiers(session, spec);
          ensureConstraint(session, spec.uniqueConstraintName(), spec.uniqueConstraintQuery());
          ensureConstraint(session, spec.requiredConstraintName(), spec.requiredConstraintQuery());
        });

        TRUSTED_PARTY_CONSTRAINTS.forEach(spec -> {
          ensureNoDuplicateTrustedPartyField(session, spec);
          ensureConstraint(session, spec.uniqueConstraintName(), spec.uniqueConstraintQuery());
          ensureConstraint(session, spec.requiredConstraintName(), spec.requiredConstraintQuery());
        });
      }
    };
  }

  private void ensureConstraint(Session session, String constraintName, String cypher) {
    try {
      session.executeWrite(tx -> {
        tx.run(cypher).consume();
        return null;
      });
      log.info("Neo4j schema ensured: {}", constraintName);
    } catch (RuntimeException ex) {
      log.error("Failed ensuring Neo4j constraint '{}'", constraintName, ex);
      throw ex;
    }
  }

  private void ensureNoDuplicateIdentifiers(Session session, IdentifierConstraintSpec spec) {
    List<String> duplicates = session.executeRead(tx -> tx.run(spec.duplicateIdentifierCheckQuery())
        .list(record -> record.get("identifier").asString() + " (count=" + record.get("count").asLong() + ")"));

    if (!duplicates.isEmpty()) {
      throw new IllegalStateException(
          "Duplicate identifiers found for label '" + spec.label() + "'. Examples: " + String.join(", ", duplicates));
    }
  }

  private void ensureNoDuplicateTrustedPartyField(Session session, TrustedPartyConstraintSpec spec) {
    List<String> duplicates = session.executeRead(tx -> tx.run("""
        MATCH (n:TrustedParty)
        WHERE n.%s IS NOT NULL
        WITH n.%s AS value, count(n) AS count
        WHERE count > 1
        RETURN value, count
        ORDER BY count DESC, value ASC
        LIMIT %d
        """.formatted(spec.onField(), spec.onField(), DUPLICATE_SAMPLE_LIMIT))
        .list(record -> record.get("value").asString() + " (count=" + record.get("count").asLong() + ")"));

    if (!duplicates.isEmpty()) {
      throw new IllegalStateException(
          "Duplicate '" + spec.onField() + "' values found for label 'TrustedParty'. Examples: "
              + String.join(", ", duplicates));
    }
  }

  private void ensureNoMissingIdentifiers(Session session, IdentifierConstraintSpec spec) {
    long missingCount = session.executeRead(tx -> tx.run(spec.missingIdentifierCountQuery())
        .single()
        .get("count")
        .asLong());

    if (missingCount > 0) {
      throw new IllegalStateException(
          "Nodes missing 'identifier' found for label '" + spec.label() + "': " + missingCount);
    }
  }

  private record IdentifierConstraintSpec(
      String label,
      String uniqueConstraintName,
      String requiredConstraintName) {

    String uniqueConstraintQuery() {
      return """
          CREATE CONSTRAINT %s IF NOT EXISTS
          FOR (n:%s)
          REQUIRE n.identifier IS UNIQUE
          """.formatted(uniqueConstraintName, label);
    }

    String requiredConstraintQuery() {
      return """
          CREATE CONSTRAINT %s IF NOT EXISTS
          FOR (n:%s)
          REQUIRE n.identifier IS NOT NULL
          """.formatted(requiredConstraintName, label);
    }

    String duplicateIdentifierCheckQuery() {
      return """
          MATCH (n:%s)
          WHERE n.identifier IS NOT NULL
          WITH n.identifier AS identifier, count(n) AS count
          WHERE count > 1
          RETURN identifier, count
          ORDER BY count DESC, identifier ASC
          LIMIT %d
          """.formatted(label, DUPLICATE_SAMPLE_LIMIT);
    }

    String missingIdentifierCountQuery() {
      return """
          MATCH (n:%s)
          WHERE n.identifier IS NULL
          RETURN count(n) AS count
          """.formatted(label);
    }
  }

  private record TrustedPartyConstraintSpec(
      String uniqueConstraintName,
      String requiredConstraintName,
      String onField) {

    String uniqueConstraintQuery() {
      return """
          CREATE CONSTRAINT %s IF NOT EXISTS
          FOR (n:TrustedParty)
          REQUIRE n.%s IS UNIQUE
          """.formatted(uniqueConstraintName, onField);
    }

    String requiredConstraintQuery() {
      return """
          CREATE CONSTRAINT %s IF NOT EXISTS
          FOR (n:TrustedParty)
          REQUIRE n.%s IS NOT NULL
          """.formatted(requiredConstraintName, onField);
    }
  }
}

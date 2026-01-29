package org.commonprovenance.framework.store.persistence.dummy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.DocumentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("dummy")
@Repository
public class DocumentDummyRepository implements DocumentRepository {
  private static Map<String, Document> documents = new HashMap<>();

  @Override
  @NotNull
  public Mono<Document> create(@NotNull Document document) {
    if (document == null)
      return Mono.error(new InternalApplicationException("Illegal argument!",
          new IllegalArgumentException("Document can not be 'null'!")));

    String identifier = document.getIdentifier().toString();
    System.out.println("*** Store doucment with id ***: " + identifier);
    documents.put(identifier, document);
    return Mono.just(documents.get(identifier));
  }

  @Override
  @NotNull
  public Flux<Document> getAll() {
    return Flux.fromIterable(documents.values());
  }

  @Override
  @NotNull
  public Mono<Document> getById(@NotNull UUID identifier) {
    if (identifier == null)
      return Mono.error(new InternalApplicationException(
          "DocumentNeo4jRepository - Error while reading document",
          new IllegalArgumentException(
              "Identifier can not be 'null'!")));
    if (!documents.containsKey(identifier.toString()))
      return Mono.empty();

    return Mono.just(documents.get(identifier.toString()));
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull UUID identifier) {
    if (identifier == null)
      return Mono.error(new InternalApplicationException(
          "DocumentNeo4jRepository - Error while deleting document",
          new IllegalArgumentException(
              "Identifier can not be 'null'!")));
    if (!documents.containsKey(identifier.toString()))
      return Mono.empty().then();
    return Mono.just(documents.remove(identifier.toString())).then();
  }
}

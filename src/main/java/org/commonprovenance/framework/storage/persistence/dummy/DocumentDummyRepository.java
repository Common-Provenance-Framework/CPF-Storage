package org.commonprovenance.framework.storage.persistence.dummy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;

import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;
import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.persistence.DocumentRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("dummy")
@Repository
public class DocumentDummyRepository implements DocumentRepository {
  private static Map<String, Document> documents = new HashMap<>();

  @Override
  @NotNull
  public Mono<Document> create(@NotNull Document document) {
    return Mono.just(documents.put(document.getIdentifier().toString(), document));
  }

  @Override
  @NotNull
  public Flux<Document> getAll() {
    return Flux.fromIterable(documents.values());
  }

  @Override
  @NotNull
  public Mono<Document> getById(@NotNull UUID identifier) {
    try {
      return Mono.just(documents.get(Objects.requireNonNull(
          identifier.toString(),
          "Identifier can not be 'null'!")));
    } catch (NullPointerException nullPointerException) {
      return Mono.error(new InternalApplicationException("Wrong identifier!", nullPointerException));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull UUID identifier) {
    try {
      return Mono.just(documents.remove(Objects.requireNonNull(
          identifier.toString(),
          "Identifier can not be 'null'!")))
          .then();
    } catch (NullPointerException nullPointerException) {
      return Mono.error(new InternalApplicationException("Wrong identifier!", nullPointerException));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }
}

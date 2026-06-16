package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import java.util.function.Function;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface OrganizationRepository {
  Mono<Void> save(Organization organization);

  Mono<Void> connectTrusts(Organization organization);

  Mono<Organization> findByIdentifier(String identifier);

  Function<Document, Mono<Void>> connectOwns(String identifier);

}

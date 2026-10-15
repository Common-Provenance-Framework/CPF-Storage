package org.commonprovenance.framework.store.queue.publisher;

import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface NroPublisher {

  Function<Organization, Mono<Organization>> issueGraphToken(String signature);

}

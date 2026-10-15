package org.commonprovenance.framework.store.queue.publisher.impl;

import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.queue.publisher.NroPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class NroPublisherImpl implements NroPublisher {
  private final RabbitTemplate rabbitTemplate;

  public NroPublisherImpl(
      RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public Function<Organization, Mono<Organization>> issueGraphToken(
      String signature
  /* rabbitmq connection parameters here */) {
    return (Organization organization) -> null;
  }

}

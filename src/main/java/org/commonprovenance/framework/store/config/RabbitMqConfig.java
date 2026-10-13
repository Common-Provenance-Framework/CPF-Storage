package org.commonprovenance.framework.store.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  public static final String NRO_EXCHANGER = "nro-exchanger";
  public static final String NRO_QUEUE_ISSUE_TOKEN = "nro-queue.token.issue";
  public static final String NRO_ROUTING_KEY_ISSUE_TOKEN = "nro-key.token.issue";

  @Bean
  public Queue nroIssueTokenQueue() {
    return new Queue(NRO_QUEUE_ISSUE_TOKEN);
  }

  @Bean
  public DirectExchange nroExchange() {
    return new DirectExchange(NRO_EXCHANGER);
  }

  @Bean
  public Binding nroIssueTokenBinding(@Qualifier("nroIssueTokenQueue") Queue queue, DirectExchange exchange) {
    return BindingBuilder
        .bind(queue)
        .to(exchange)
        .with(NRO_ROUTING_KEY_ISSUE_TOKEN);
  }

  @Bean
  public MessageConverter messageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public AmqpTemplate template(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());
    return template;
  }
}

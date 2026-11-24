package org.commonprovenance.framework.store.web.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationRegisterFormDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationUpdateFormDTO;
import org.commonprovenance.framework.store.web.client.reactive.ClientReactive;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientReactive Spec")
public class ClientReactiveSpec {
  private String URL = "http://example.test";
  @Mock
  private WebConfig config;

  private ListAppender<ILoggingEvent> logAppender;
  private Logger clientLogger;
  private Level originalLogLevel;

  @BeforeEach
  void setUpLogCapture() {
    clientLogger = (Logger) LoggerFactory.getLogger(ClientReactive.class);
    originalLogLevel = clientLogger.getLevel();
    clientLogger.setLevel(Level.TRACE);
    logAppender = new ListAppender<>();
    logAppender.start();
    clientLogger.addAppender(logAppender);
  }

  @AfterEach
  void tearDownLogCapture() {
    clientLogger.detachAppender(logAppender);
    clientLogger.setLevel(originalLogLevel);
  }

  @Test
  void sendHeadRequest() {
    WebClient webClient = mock(WebClient.class);
    WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(uriSpec).when(webClient)
        .head();

    doReturn(headersSpec).when(uriSpec)
        .uri(ArgumentMatchers.<Function<UriBuilder, URI>> any());

    doReturn(responseSpec).when(headersSpec)
        .retrieve();

    when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

    ClientReactive client = new ClientReactive(webClient, webClient, webClient, config);

    client.sendHeadRequest(webClient, "/resource", Map.of("arg1", "val1", "arg2", "val2"))
        .block();

    verify(webClient).head();

    ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.captor();
    verify(uriSpec).uri(uriCaptor.capture());
    URI actualUri = uriCaptor.getValue()
        .apply(new DefaultUriBuilderFactory(URL).builder());
    assertEquals(
        "http://example.test/resource?arg1=val1&arg2=val2",
        actualUri.toString());
    verify(responseSpec).bodyToMono(Void.class);
  }

  @Test
  void sendGetOneRequest() {
    OrganizationResponseDTO response = mock(OrganizationResponseDTO.class);

    WebClient webClient = mock(WebClient.class);
    WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(uriSpec).when(webClient)
        .get();

    doReturn(headersSpec).when(uriSpec)
        .uri(ArgumentMatchers.<Function<UriBuilder, URI>> any());

    doReturn(responseSpec).when(headersSpec)
        .retrieve();

    when(responseSpec.bodyToMono(OrganizationResponseDTO.class)).thenReturn(Mono.just(response));

    ClientReactive client = new ClientReactive(webClient, webClient, webClient, config);

    client.sendGetOneRequest(webClient, "/resource", OrganizationResponseDTO.class, Map.of("arg1", "val1", "arg2", "val2"))
        .block();

    verify(webClient).get();

    ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.captor();
    verify(uriSpec).uri(uriCaptor.capture());
    URI actualUri = uriCaptor.getValue()
        .apply(new DefaultUriBuilderFactory(URL).builder());
    assertEquals(
        "http://example.test/resource?arg1=val1&arg2=val2",
        actualUri.toString());
    verify(responseSpec).bodyToMono(OrganizationResponseDTO.class);
  }

  @Test
  void sendGetManyRequest() {
    OrganizationResponseDTO response = mock(OrganizationResponseDTO.class);

    WebClient webClient = mock(WebClient.class);
    WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(uriSpec).when(webClient)
        .get();

    doReturn(headersSpec).when(uriSpec)
        .uri(ArgumentMatchers.<Function<UriBuilder, URI>> any());

    doReturn(responseSpec).when(headersSpec)
        .retrieve();

    when(responseSpec.bodyToFlux(OrganizationResponseDTO.class)).thenReturn(Flux.just((response)));

    ClientReactive client = new ClientReactive(webClient, webClient, webClient, config);

    client.sendGetManyRequest(webClient, "/resource", OrganizationResponseDTO.class, Map.of("arg1", "val1", "arg2", "val2"))
        .collectList()
        .block();

    verify(webClient).get();

    ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.captor();
    verify(uriSpec).uri(uriCaptor.capture());
    URI actualUri = uriCaptor.getValue()
        .apply(new DefaultUriBuilderFactory(URL).builder());
    assertEquals(
        "http://example.test/resource?arg1=val1&arg2=val2",
        actualUri.toString());
    verify(responseSpec).bodyToFlux(OrganizationResponseDTO.class);
  }

  @Test
  void sendPostRequest() {
    OrganizationRegisterFormDTO payload = mock(OrganizationRegisterFormDTO.class);

    WebClient webClient = mock(WebClient.class);
    WebClient.RequestBodyUriSpec bodySpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(bodySpec).when(webClient)
        .post();

    doReturn(bodySpec).when(bodySpec)
        .uri(ArgumentMatchers.<Function<UriBuilder, URI>> any());

    doReturn(headersSpec).when(bodySpec)
        .bodyValue(any(OrganizationRegisterFormDTO.class));

    doReturn(responseSpec).when(headersSpec)
        .retrieve();

    when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

    ClientReactive client = new ClientReactive(webClient, webClient, webClient, config);

    client.sendPostRequest(webClient, "/resource", Void.class, Map.of("arg1", "val1", "arg2", "val2"))
        .apply(payload)
        .block();

    verify(webClient).post();
    ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.captor();
    verify(bodySpec).uri(uriCaptor.capture());
    URI actualUri = uriCaptor.getValue()
        .apply(new DefaultUriBuilderFactory(URL).builder());
    assertEquals(
        "http://example.test/resource?arg1=val1&arg2=val2",
        actualUri.toString());
    verify(bodySpec).bodyValue(payload);
    verify(responseSpec).bodyToMono(Void.class);
  }

  @Test
  void sendPutRequest() {
    OrganizationUpdateFormDTO payload = mock(OrganizationUpdateFormDTO.class);

    WebClient webClient = mock(WebClient.class);
    WebClient.RequestBodyUriSpec bodySpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(bodySpec).when(webClient)
        .put();

    doReturn(bodySpec).when(bodySpec)
        .uri(ArgumentMatchers.<Function<UriBuilder, URI>> any());

    doReturn(headersSpec).when(bodySpec)
        .bodyValue(any(OrganizationUpdateFormDTO.class));

    doReturn(responseSpec).when(headersSpec)
        .retrieve();

    when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

    ClientReactive client = new ClientReactive(webClient, webClient, webClient, config);

    client.sendPutRequest(webClient, "/resource", Void.class, Map.of("arg1", "val1", "arg2", "val2"))
        .apply(payload)
        .block();

    verify(webClient).put();
    ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.captor();
    verify(bodySpec).uri(uriCaptor.capture());
    URI actualUri = uriCaptor.getValue()
        .apply(new DefaultUriBuilderFactory(URL).builder());
    assertEquals(
        "http://example.test/resource?arg1=val1&arg2=val2",
        actualUri.toString());
    verify(bodySpec).bodyValue(payload);
    verify(responseSpec).bodyToMono(Void.class);
  }

  @Test
  void sendDeleteRequest() {
    WebClient webClient = mock(WebClient.class);
    WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(uriSpec).when(webClient)
        .delete();

    doReturn(headersSpec).when(uriSpec)
        .uri(ArgumentMatchers.<Function<UriBuilder, URI>> any());

    doReturn(responseSpec).when(headersSpec)
        .retrieve();

    when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

    ClientReactive client = new ClientReactive(webClient, webClient, webClient, config);

    client.sendDeleteRequest(webClient, "/resource", Void.class, Map.of("arg1", "val1", "arg2", "val2"))
        .block();

    verify(webClient).delete();

    ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.captor();
    verify(uriSpec).uri(uriCaptor.capture());
    URI actualUri = uriCaptor.getValue()
        .apply(new DefaultUriBuilderFactory(URL).builder());
    assertEquals(
        "http://example.test/resource?arg1=val1&arg2=val2",
        actualUri.toString());
    verify(responseSpec).bodyToMono(Void.class);
  }
}

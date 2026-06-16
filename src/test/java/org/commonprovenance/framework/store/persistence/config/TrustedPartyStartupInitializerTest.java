package org.commonprovenance.framework.store.persistence.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationRunner;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrustedParty startup initializer unit tests")
class TrustedPartyStartupInitializerTest {

  @Mock
  private TrustedPartyService trustedPartyService;

  @Mock
  private TrustedPartyWeb trustedPartyWeb;

  private ApplicationRunner runner;

  private TrustedParty defaultTrustedParty;

  @BeforeEach
  void setUp() {
    TrustedPartyStartupInitializer initializer = new TrustedPartyStartupInitializer();
    runner = initializer.ensureDefaultTrustedParty(trustedPartyService, trustedPartyWeb);

    defaultTrustedParty = new TrustedParty(
        "default",
        "certificate",
        "http://localhost:8093/api/v1",
        true,
        true,
        true);
  }

  @Test
  void should_not_create_default_trusted_party_when_it_already_exists() throws Exception {
    when(trustedPartyService.getDefaultTrustedParty()).thenReturn(Mono.just(defaultTrustedParty));

    runner.run(null);

    verify(trustedPartyService).getDefaultTrustedParty();
    verify(trustedPartyWeb, never()).getTrustedParty(Optional.empty());
    verify(trustedPartyService, never()).storeTrustedParty(defaultTrustedParty);
  }

  @Test
  void should_create_default_trusted_party_when_missing() throws Exception {
    when(trustedPartyService.getDefaultTrustedParty()).thenReturn(Mono.error(new NotFoundException("missing")));
    when(trustedPartyWeb.getTrustedParty(Optional.empty())).thenReturn(Mono.just(defaultTrustedParty));
    when(trustedPartyService.storeTrustedParty(defaultTrustedParty)).thenReturn(Mono.empty());

    runner.run(null);

    verify(trustedPartyService).getDefaultTrustedParty();
    verify(trustedPartyWeb).getTrustedParty(Optional.empty());
    verify(trustedPartyService).storeTrustedParty(defaultTrustedParty);
  }

  @Test
  void should_fail_startup_when_default_trusted_party_fetch_fails() {
    when(trustedPartyService.getDefaultTrustedParty()).thenReturn(Mono.error(new NotFoundException("missing")));
    when(trustedPartyWeb.getTrustedParty(Optional.empty())).thenReturn(Mono.error(new IllegalStateException("remote unavailable")));

    assertThrows(IllegalStateException.class, () -> runner.run(null));

    verify(trustedPartyService).getDefaultTrustedParty();
    verify(trustedPartyWeb).getTrustedParty(Optional.empty());
  }

  @Test
  void should_fail_startup_when_default_trusted_party_store_fails() {
    when(trustedPartyService.getDefaultTrustedParty()).thenReturn(Mono.error(new NotFoundException("missing")));
    when(trustedPartyWeb.getTrustedParty(Optional.empty())).thenReturn(Mono.just(defaultTrustedParty));
    when(trustedPartyService.storeTrustedParty(defaultTrustedParty)).thenReturn(Mono.error(new IllegalStateException("store failed")));

    assertThrows(IllegalStateException.class, () -> runner.run(null));

    verify(trustedPartyService).getDefaultTrustedParty();
    verify(trustedPartyWeb).getTrustedParty(Optional.empty());
    verify(trustedPartyService).storeTrustedParty(defaultTrustedParty);
  }
}

package org.commonprovenance.framework.store.web.trustedParty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.OrganizationFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.RegisterOrganizationFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.UpdateOrganizationFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.impl.OrganizationWebImpl;
import org.commonprovenance.framework.store.web.trustedParty.impl.TrustedPartyWebImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationWeb Spec")
class OrganizationWebImplSpec {

  private static final String DEFAULT_TP_URL = "http://localhost:8020/api/v1";
  private static final String TP_URL = "http://localhost:80/api/v1";

  @Mock
  private Client client;

  @Mock
  private WebClient defaultTpWebClient;

  @Mock
  private WebClient customTpWebClient;

  @Mock
  private Organization organization;

  @Mock
  private TrustedParty trustedParty;

  private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7b";
  private OrganizationWeb organizationWeb;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger storeWebLogger;

  @BeforeEach
  void setUp() {
    organizationWeb = new OrganizationWebImpl(client);
    when(client.getDefaultTrustedPartyUrl()).thenReturn(DEFAULT_TP_URL);
  }

  @BeforeEach
  void setUpLogCapture() {
    storeWebLogger = (Logger) LoggerFactory.getLogger(TrustedPartyWebImpl.class);
    storeWebLogger.setLevel(Level.TRACE); // success messages are logged at TRACE
    logAppender = new ListAppender<>();
    logAppender.start();
    storeWebLogger.addAppender(logAppender);
  }

  @AfterEach
  void tearDownLogCapture() {
    storeWebLogger.detachAppender(logAppender);
  }

  @Nested
  @DisplayName("create")
  class Create {
    @Mock
    private RegisterOrganizationFormDTO registrationFormDTO;

    private String CREATE_URI = "/organizations/" + ORGANIZATION_ID;

    @Test
    @DisplayName("Create (Register) new Organization on default TrustedParty")
    void create_default() {
      createNewOrganization(true);
    }

    @Test
    @DisplayName("Create (Register) new Organization on custom TrustedParty")
    void create_custom() {
      createNewOrganization(false);
    }

    private void createNewOrganization(Boolean isDefault) {
      WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
      Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

      try (MockedStatic<RegisterOrganizationFormFactory> registerFormFactory = mockStatic(RegisterOrganizationFormFactory.class)) {

        when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
        when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));

        when(trustedParty.getUrlIfNotDefault()).thenReturn(maybeUrl);

        registerFormFactory.when(() -> RegisterOrganizationFormFactory.buildSafe(any(Organization.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              Organization org = invocation.getArgument(0);
              assertEquals(organization, org, "Factory should receive exact organization");

              return Either.right(registrationFormDTO);
            });

        when(client.trustedParty(any())).thenAnswer((InvocationOnMock invocation) -> {
          Optional<String> tpUrl = invocation.getArgument(0);
          return tpUrl.isPresent() ? customTpWebClient : defaultTpWebClient;
        });
        when(client.sendPostRequest(any(WebClient.class), anyString(), any(), anyMap()))
            .thenAnswer(_ -> (Function<RegisterOrganizationFormDTO, Mono<Void>>) form -> {
              assertEquals(registrationFormDTO, form, "Request should contain the register organization form");
              return Mono.empty();
            });

        organizationWeb.create(organization)
            .block();

        registerFormFactory.verify(() -> RegisterOrganizationFormFactory.buildSafe(organization));
        verify(client).trustedParty(maybeUrl);
        verify(client).sendPostRequest(webClient, CREATE_URI, Void.class, Map.of());
      }
    }

  }

  @Nested
  @DisplayName("updateOrganizationCertificate")
  class UpdateOrganizationCertificate {
    @Mock
    private UpdateOrganizationFormDTO updateFormDTO;

    private String UPDATE_URI = "/organizations/" + ORGANIZATION_ID + "/certs";

    @Test
    @DisplayName("Update Organization on default TrustedParty")
    void update_default() {
      updateOrganization(true);
    }

    @Test
    @DisplayName("Update Organization on custom TrustedParty")
    void update_custom() {
      updateOrganization(false);
    }

    private void updateOrganization(Boolean isDefault) {
      WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
      Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

      try (MockedStatic<UpdateOrganizationFormFactory> updateFormFactory = mockStatic(UpdateOrganizationFormFactory.class)) {

        when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
        when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));

        when(trustedParty.getUrlIfNotDefault()).thenReturn(maybeUrl);

        updateFormFactory.when(() -> UpdateOrganizationFormFactory.buildSafe(any(Organization.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              Organization org = invocation.getArgument(0);
              assertEquals(organization, org, "Factory should receive exact organization");

              return Either.right(updateFormDTO);
            });

        when(client.trustedParty(any())).thenAnswer((InvocationOnMock invocation) -> {
          Optional<String> tpUrl = invocation.getArgument(0);
          return tpUrl.isPresent() ? customTpWebClient : defaultTpWebClient;
        });
        when(client.sendPutRequest(any(WebClient.class), anyString(), any(), anyMap()))
            .thenAnswer(_ -> (Function<UpdateOrganizationFormDTO, Mono<Void>>) form -> {
              assertEquals(updateFormDTO, form, "Request should contain the update organization form");
              return Mono.empty();
            });

        organizationWeb.updateOrganizationCertificate(organization)
            .block();

        updateFormFactory.verify(() -> UpdateOrganizationFormFactory.buildSafe(organization));
        verify(client).trustedParty(maybeUrl);
        verify(client).sendPutRequest(webClient, UPDATE_URI, Void.class, Map.of());
      }
    }

  }

  @Nested
  @DisplayName("getById")
  class GetById {
    @Mock
    private OrganizationResponseDTO responseDTO;

    private String GET_URI = "/organizations/" + ORGANIZATION_ID;

    @Test
    @DisplayName("Get Organization from default TrustedParty")
    void get_default() {
      getOrganization(true);
    }

    @Test
    @DisplayName("Get Organization from custom TrustedParty")
    void get_custom() {
      getOrganization(false);
    }

    private void getOrganization(Boolean isDefault) {
      WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
      Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

      try (MockedStatic<OrganizationFactory> organizationFactory = mockStatic(OrganizationFactory.class)) {

        when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
        when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));

        when(trustedParty.getUrlIfNotDefault()).thenReturn(maybeUrl);

        organizationFactory.when(() -> OrganizationFactory.buildUnsafe(any(OrganizationResponseDTO.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              OrganizationResponseDTO dto = invocation.getArgument(0);
              assertEquals(responseDTO, dto, "Factory should receive exact organization response DTO");

              return Either.right(organization);
            });

        when(client.trustedParty(any())).thenAnswer((InvocationOnMock invocation) -> {
          Optional<String> tpUrl = invocation.getArgument(0);
          return tpUrl.isPresent() ? customTpWebClient : defaultTpWebClient;
        });

        when(client.sendGetOneRequest(any(WebClient.class), anyString(), any(), anyMap()))
            .thenReturn(Mono.just(responseDTO));

        organizationWeb.getById(organization)
            .block();

        verify(client).trustedParty(maybeUrl);
        verify(client).sendGetOneRequest(webClient, GET_URI, OrganizationResponseDTO.class, Map.of());
        organizationFactory.verify(() -> OrganizationFactory.buildUnsafe(responseDTO));
      }
    }

  }

}

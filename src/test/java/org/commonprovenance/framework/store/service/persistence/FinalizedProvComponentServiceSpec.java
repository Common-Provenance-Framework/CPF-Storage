package org.commonprovenance.framework.store.service.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyRepository;
import org.commonprovenance.framework.store.service.persistence.impl.FinalizedProvComponentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openprovenance.prov.vanilla.QualifiedName;

import cz.muni.fi.cpm.model.CpmDocument;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service - FinalizedProvComponentServiceImpl Specification")
class FinalizedProvComponentServiceSpec {

  @Mock
  private OrganizationRepository organizationRepository;
  @Mock
  private TrustedPartyRepository trustedPartyRepository;
  @Mock
  private DocumentRepository documentRepository;
  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private CpmDocument cpmDocument;

  @Mock
  private Organization organization;

  @Mock
  private Token token;

  @Mock
  private TrustedParty trustedParty;

  private FinalizedProvComponentServiceImpl finalizedProvComponentServiceImpl;

  private final String ORG_ID = "org_01";

  private final String UUID_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final GraphFormat FORMAT_1 = GraphFormat.JSON;

  private final String TP_NAME = "TrustedParty";

  @BeforeEach
  void setUp() {
    finalizedProvComponentServiceImpl = new FinalizedProvComponentServiceImpl(
        organizationRepository,
        trustedPartyRepository,
        documentRepository,
        tokenRepository);
  }

  @Test
  @DisplayName("storeDocument - should call create on repository once with exact Document.")

  void storeDocument_should_call_create_on_repository() {
    final Document document = new Document(BASE64_STRING_GRAPH_1, FORMAT_1, cpmDocument, token);

    when(organization.getDocument()).thenReturn(Optional.of(document));
    when(organization.getIdentifier()).thenReturn(ORG_ID);
    when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
    when(trustedParty.getName()).thenReturn(TP_NAME);
    when(cpmDocument.getBundleId()).thenReturn(new QualifiedName(
        "http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/",
        UUID_1,
        "storage"));

    when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
      Document capturedDocument = invocation.getArgument(0);

      assertEquals(
          capturedDocument.getIdentifier().get(),
          UUID_1,
          "should be called with exact Document");
      return Mono.empty();
    });

    when(organizationRepository.connectOwns(any(String.class))).thenAnswer(invocation -> {
      String identifier = invocation.getArgument(0);
      assertEquals(
          ORG_ID,
          identifier,
          "should be called with exact Organization identifier");

      return (Function<Document, Mono<Void>>) doc -> {
        assertEquals(UUID_1,
            doc.getIdentifier().get(),
            "should be called with exact Document");
        return Mono.empty();
      };
    });

    when(tokenRepository.connectWasIssuedBy(any())).thenAnswer(invocation -> {
      Optional<TrustedParty> maybeTrustedParty = invocation.getArgument(0);
      assertTrue(maybeTrustedParty.isPresent(), "should not be empty");
      assertEquals(
          TP_NAME,
          maybeTrustedParty.get().getName(),
          "should be called with exact TrusteParty");

      return (Function<Document, Mono<Void>>) doc -> {
        assertEquals(UUID_1,
            doc.getIdentifier().get(),
            "should be called with exact Document");
        return Mono.empty();
      };
    });

    StepVerifier.create(finalizedProvComponentServiceImpl.storeDocument(organization))
        .verifyComplete();

    verify(
        documentRepository,
        times(1)
            .description("Repository save method should be invoked once"))
        .save(any(Document.class));

    verify(
        organizationRepository,
        times(1)
            .description("Repository connectOwns method should be invoked once"))
        .connectOwns(any(String.class));

    verify(
        tokenRepository,
        times(1)
            .description("Repository connectWasIssuedBy method should be invoked once"))
        .connectWasIssuedBy(any());
  }

  @Test
  void getDocumentByIdentifier_shouldReturnDocument() {
    Document document = new Document(BASE64_STRING_GRAPH_1, FORMAT_1);

    when(documentRepository.findByIdentifier(any(String.class))).thenAnswer(invocation -> {
      String identifier = invocation.getArgument(0);

      assertEquals(
          UUID_1,
          identifier,
          "should be called with exact Document identifier");

      return Mono.just(document);
    });

    StepVerifier.create(finalizedProvComponentServiceImpl.getDocumentByIdentifier(UUID_1))
        .expectNext(document)
        .verifyComplete();

    verify(
        documentRepository,
        times(1)
            .description("Repository getByIdentifier method should be invoked once"))
        .findByIdentifier(anyString());

  }

}

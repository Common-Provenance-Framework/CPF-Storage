package org.commonprovenance.framework.store.service.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
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

@DisplayName("Service - DocumentServiceImpl UnitTest")
@ExtendWith(MockitoExtension.class)
class FinalizedProvComponentServiceTest {
  @Mock
  private Organization organization;

  @Mock
  private TrustedParty trustedParty;

  @Mock
  private Token token;

  @Mock
  private CpmDocument cpmDocument;

  final private String ORG_ID = "ORG_01";
  private final String TP_NAME = "TrustedParty";
  private final String UUID_ERR_TRIGER = "...";

  final private String UUID_STR_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
  final private String TEST_ORG_ID_1 = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
  final private String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  final private GraphFormat FORMAT_1 = GraphFormat.JSON;

  private Document DOCUMENT_1;

  final private String UUID_STR_2 = "dc3b1fc8-d01e-4405-8cf8-94320a11ba4c";
  final private String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
  final private GraphFormat FORMAT_2 = GraphFormat.JSON;
  private Document DOCUMENT_2;

  private class OrganizationRepositoryStub implements OrganizationRepository {

    @Override
    public Mono<Void> save(Organization organization) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Mono<Void> connectTrusts(Organization organization) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'connectTrusts'");
    }

    @Override
    public Mono<Organization> findByIdentifier(String identifier) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'findByIdentifier'");
    }

    @Override
    public Mono<Boolean> existsByIdentifier(String identifier) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'existsByIdentifier'");
    }

    @Override
    public Function<Document, Mono<Void>> connectOwns(String identifier) {
      assertEquals(
          ORG_ID,
          identifier,
          "should be called with exact Organization identifier");
      return (Document doc) -> {
        assertEquals(
            UUID_STR_1,
            doc.getIdentifier().get(),
            "should be called with exact Document");

        return Mono.empty();
      };
    }
  }

  private class TrustedPartyRepositoryStub implements TrustedPartyRepository {

    @Override
    public Mono<Void> create(TrustedParty trustedParty) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public Mono<TrustedParty> findByName(String name) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'findByName'");
    }

    @Override
    public Mono<TrustedParty> findDefault() {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'findDefault'");
    }

    @Override
    public Mono<TrustedParty> findByOrganizationIdentifier(String organizationIdentifier) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'findByOrganizationIdentifier'");
    }

    @Override
    public Mono<String> findUrlByOrganizationIdentifier(String organizationIdentifier) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'findUrlByOrganizationIdentifier'");
    }

  }

  private class DocumentRepositoryStub implements DocumentRepository {

    @Override
    public Mono<Void> save(Document document) {
      return document == null
          ? Mono.error(new InternalApplicationException("Illegal argument!",
              new IllegalArgumentException("Document can not be 'null'!")))
          : Mono.empty();
    }

    @Override
    public Mono<Document> findByIdentifier(String identifier) {

      if (identifier == UUID_ERR_TRIGER)
        return Mono.error(new InternalApplicationException(
            "Search Document with identifier '" + identifier + "' has been failed!",
            new IllegalArgumentException("Identifier can not be 'null'!")));

      switch (identifier) {
        case UUID_STR_1:
          return Mono.just(DOCUMENT_1);
        case UUID_STR_2:
          return Mono.just(DOCUMENT_2);
        default:
          return Mono.error(() -> new NotFoundException("Document with identifier '" + identifier + "' has not been found!"));
      }
    }

    @Override
    public Mono<Boolean> existsByIdentifier(String identifier) {
      if (identifier == null)
        return Mono.error(new InternalApplicationException(
            "DocumentNeo4jRepository - Error while reading document",
            new IllegalArgumentException(
                "Identifier can not be 'null'!")));

      switch (identifier) {
        case UUID_STR_1:
          return Mono.just(true);
        case UUID_STR_2:
          return Mono.just(true);
        default:
          return Mono.just(false);
      }
    }

    @Override
    public Mono<String> getOrganizationIdentifierByIdentifier(String identifier) {
      return Mono.just(TEST_ORG_ID_1);
    }
  }

  private class TokenRepositoryStub implements TokenRepository {

    @Override
    public Mono<Void> save(Token token) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Mono<Token> getTokenByDocumentIdentifier(String documentIdentifier) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'getTokenByDocumentIdentifier'");
    }

    @Override
    public Mono<String> getTokenIdByDocumentIdentifier(String documentIdentifier) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'getTokenIdByDocumentIdentifier'");
    }

    @Override
    public Function<Document, Mono<Void>> connectWasIssuedBy(Optional<TrustedParty> maybeTrustedParty) {
      assertTrue(maybeTrustedParty.isPresent(), "should not be empty");
      assertEquals(
          TP_NAME,
          maybeTrustedParty.get().getName(),
          "should be called with exact TrusteParty");

      return (Function<Document, Mono<Void>>) doc -> {
        assertEquals(UUID_STR_1,
            doc.getIdentifier().get(),
            "should be called with exact Document");
        return Mono.empty();
      };
    }

  }

  private final OrganizationRepository organizationRepository;
  private final TrustedPartyRepository trustedPartyRepository;
  private final DocumentRepository documentRepository;
  private final TokenRepository tokenRepository;

  private FinalizedProvComponentService finalizedProvComponentService;

  public FinalizedProvComponentServiceTest() {
    this.organizationRepository = new OrganizationRepositoryStub();
    this.trustedPartyRepository = new TrustedPartyRepositoryStub();
    this.documentRepository = new DocumentRepositoryStub();
    this.tokenRepository = new TokenRepositoryStub();
  }

  @BeforeEach
  void setUp() {
    finalizedProvComponentService = new FinalizedProvComponentServiceImpl(
        organizationRepository,
        trustedPartyRepository,
        documentRepository,
        tokenRepository);

    DOCUMENT_1 = new Document(BASE64_STRING_GRAPH_1, FORMAT_1, cpmDocument, token);
    DOCUMENT_2 = new Document(BASE64_STRING_GRAPH_2, FORMAT_2);
  }

  @Test
  @DisplayName("HappyPath - storeDocument - should return new Document which has been stored.")
  void storeDocument_return_new_document() {

    when(organization.getIdentifier()).thenReturn(ORG_ID);
    when(organization.getDocument()).thenReturn(Optional.of(DOCUMENT_1));
    when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
    when(trustedParty.getName()).thenReturn(TP_NAME);

    when(cpmDocument.getBundleId()).thenReturn(new QualifiedName(
        "http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/",
        UUID_STR_1,
        "storage"));

    StepVerifier.create(finalizedProvComponentService.storeDocument(organization))
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getDocumentById - should return Mono with exact document from repository.")
  void getDocumentByIdentifier_should_return_mono_with_exact_document() {
    when(cpmDocument.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName(
          "http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/",
          UUID_STR_1,
          "storage");
    });
    StepVerifier.create(finalizedProvComponentService.getDocumentByIdentifier(UUID_STR_1))
        .assertNext(doc -> {
          assertEquals(UUID_STR_1, doc.getIdentifier().get(),
              "should return Mono with exact Document");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getDocumentByIdentifier - should return empty Mono.")
  void getDocumentByIdentifier_should_return_empty_mono() {

    String docIdentifier = "doc_id";
    StepVerifier.create(finalizedProvComponentService.getDocumentByIdentifier(docIdentifier))
        .verifyErrorSatisfies(exception -> {
          assertInstanceOf(NotFoundException.class,
              exception,
              "should be NotFoundException - Exception");
          assertEquals(
              "Document with identifier '" + docIdentifier + "' has not been found!",
              exception.getMessage(),
              "should have exact error message");

          assertNull(
              exception.getCause(),
              "should not have cause");
        });
  }

  @Test
  @DisplayName("ErrorPath - getDocumentByIdentifier - should propagate exception from repository, if any.")
  void getDocumentByIdentifier_propagete_exception_from_repository() {

    StepVerifier.create(finalizedProvComponentService.getDocumentByIdentifier(UUID_ERR_TRIGER))
        .verifyErrorSatisfies(err -> {

          assertInstanceOf(InternalApplicationException.class,
              err,
              "should be InternalApplicationException - Exception");

          assertEquals(
              "Search Document with identifier '" + UUID_ERR_TRIGER + "' has been failed!",
              err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              IllegalArgumentException.class,
              err.getCause(),
              "should be IllegalArgumentException - Exception cause");
          assertEquals(
              "Identifier can not be 'null'!",
              err.getCause().getMessage(),
              "should have exact error message");
        });
  }
}

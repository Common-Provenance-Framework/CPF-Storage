package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.AdditionalData;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Controller - DTO Mapper (Model to Response)")
public class DTOFactoryTest {
  @Test
  @DisplayName("HappyPath - should return Mono with DocumentResponseDTO")
  void should_map_Document_to_DocumentResponseDTO() {
    String testId = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
    String organizationId = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "JSON";
    String signature = "..";

    String cert = """
        -----BEGIN CERTIFICATE-----
        MIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw
        bTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug
        RGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp
        Y2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd
        MQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE
        ZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw
        EwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9
        +KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD
        VR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji
        XokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ
        MAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1
        FRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=
        -----END CERTIFICATE-----
        """;

    AdditionalData additionalData = new AdditionalData(
        "http://localhost:8080/api/v1/documents/DnaSequencingBundle_V0",
        "6fb292aa-ee38-48ae-998f-079ad9d01e7c",
        "SHA256",
        "https://trusted-party.org/",
        cert,
        1774953179L);

    TrustedParty trustedParty = new TrustedParty(
        "Trusted_Party",
        "Trusted_Party",
        cert,
        "https://trusted-party.org/",
        true,
        true,
        true);

    Document document = new Document(
        testId,
        organizationId,
        base64StringGraph,
        Format.from(format).get(),
        signature);

    Token token = new Token(
        "411ad4a53083e377b1eceb38bdeea7ea15289fe68b19d95ea49607ae172e694f",
        "MEQCICcnuE4gQhuE5fmqzDLfKDe/ok0XClMRsjLYuRVdscgwAiBBso51AFdySZOPhJo62hIDWvCf5G67JJY6YQMveDLeVA==",
        additionalData,
        trustedParty,
        document,
        1774953179L);
    StepVerifier.create(DTOFactory.toDocumentDTO(token))
        .assertNext((DocumentResponseDTO response) -> {
          assertEquals(base64StringGraph, response.getDocument(),
              "response should have document field with exact value");

          assertEquals(token.getSignature(), response.getToken().getSignature(),
              "response should have token field with exact signature");

          // TODO: ...
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact error")
  void should_return_Mono_with_error() {

    StepVerifier.create(DTOFactory.toDocumentDTO((Token) null))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Input parameter can not be null.", error.getMessage());
          assertNull(error.getCause());
          // assertInstanceOf(InternalApplicationException.class, error.getCause());
          // assertNull(error.getCause().getMessage());
        })
        .verify();
  }
}

package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
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

    String jwt = "eyJhbGciOiJFUzI1NiIsImJ1bmRsZSI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9hcGkvdjEvZG9jdW1lbnRzL2RjOGVmZWQwLTAwMzUtNDAyOS05MDY1LThjNDY2NjcxNTFkYiIsImhhc2hGdW5jdGlvbiI6IlNIQTI1NiIsInRydXN0ZWRQYXJ0eUNlcnRpZmljYXRlIjoiLS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tXG5NSUlDTWpDQ0FkaWdBd0lCQWdJVVNMajVZN1BYSVMxM3FQRVBEZGxJTkJuUXpvZ3dDZ1lJS29aSXpqMEVBd0l3XG5iVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnXG5SR1Z0YnlCRFpYSjBhV1pwWTJGMFpTQkJkWFJvYjNKcGRIa3hJakFnQmdOVkJBTU1HVVJRUkNCRFpYSjBhV1pwXG5ZMkYwWlNCQmRYUm9iM0pwZEhrd0hoY05NalF4TVRFMk1ESTFPVFV5V2hjTk16UXhNVEUwTURJMU9UVXlXakJkXG5NUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFXG5aVzF2SUZSeWRYTjBaV1FnVUdGeWRIa3hHakFZQmdOVkJBTU1FVVJRUkNCVWNuVnpkR1ZrSUZCaGNuUjVNRmt3XG5Fd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRStWOGtUNGprdkVXbVgzMDFLQVM5ZWtsbW5STmk2Z1U5XG4rS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEXG5WUjBUQVFIL0JBZ3dCZ0VCL3dJQkFEQU9CZ05WSFE4QkFmOEVCQU1DQVlZd0hRWURWUjBPQkJZRUZNQ25QUmppXG5Yb2tUN3F1d1pSQjE2QUFnejdibk1COEdBMVVkSXdRWU1CYUFGQ3lFS3dpMWp2ZFBxZmlVK05kSC9udmg3UFlaXG5NQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxXG5GUkFaWWdJZ1ZoNFMydFFuMTJYWWRkNUlTc0NwQUJzaDZacmpTaVZZcnQyVDFPMW5Rc3c9XG4tLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tXG4iLCJ0cnVzdGVkUGFydHlVcmkiOiJ0cnVzdGVkLXBhcnR5OjgwMjAiLCJ0eXAiOiJKV1QifQ.eyJhdXRob3JpdHlJZCI6IlRydXN0ZWRfUGFydHkiLCJkb2N1bWVudENyZWF0aW9uVGltZXN0YW1wIjoxNzc2MDc5OTM5LCJkb2N1bWVudERpZ2VzdCI6ImNiMjRlMTYxYzAxM2QxZTE5MjNhZmI0MWU2ODEyYWQ0OGU4MTM1MGQwNDM1MTE4MmFlZWNhYjRiZDY1YzJjY2EiLCJvcmlnaW5hdG9ySWQiOiI2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0wNzlhZDlkMDFlN2MiLCJ0b2tlblRpbWVzdGFtcCI6MTc3NjA3OTkzOX0.jsJt6Ny17qpN3ynMg3HBVXHWLc6b7yFfUEkGnsqjyscw3rsUYKKRv-BpAlarBdVLUYWK9IelRehha88KwOr89w";

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
        jwt,
        trustedParty,
        document,
        1774953179L);

    StepVerifier.create(DTOFactory.toDocumentDTO(token))
        .assertNext((DocumentResponseDTO response) -> {
          assertEquals(base64StringGraph, response.getDocument(),
              "response should have document field with exact value");

          assertEquals(jwt, response.getToken().getJwt(),
              "response should have token field with exact jwt");
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
        })
        .verify();
  }
}

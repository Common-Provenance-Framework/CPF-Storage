package org.commonprovenance.framework.storage.controller.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.UUID;

import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;
import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.model.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Controller - DTO Mapper (Model to Response)")
public class DTOMapperTest {
    @Test
    @DisplayName("HappyPath - should return Mono with DocumentResponseDTO")
    void should_map_Document_to_DocumentResponseDTO() {
        String testId = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
        String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
        String format = "JSON";

        Document document = new Document(
                UUID.fromString(testId),
                base64StringGraph,
                Format.from(format).get());

        StepVerifier.create(DTOMapper.toDTO(document))
                .assertNext(response -> {
                    assertEquals(testId, response.getIdentifier(),
                            "response should have identifier field with exact value");
                    assertEquals(base64StringGraph, response.getGraph(),
                            "response should have graph field with exact value");
                    assertEquals(format, response.getFormat(),
                            "response should have format field with exact value");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("ErrorPath - should return Mono with exact error")
    void should_return_Mono_with_error() {

        StepVerifier.create(DTOMapper.toDTO(null))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(InternalApplicationException.class, error);
                    assertEquals("Can not convert to DocumentResponseDTO", error.getMessage());
                    assertInstanceOf(IllegalArgumentException.class, error.getCause());
                    assertEquals("Document can not be null!", error.getCause().getMessage());
                })
                .verify();
    }
}

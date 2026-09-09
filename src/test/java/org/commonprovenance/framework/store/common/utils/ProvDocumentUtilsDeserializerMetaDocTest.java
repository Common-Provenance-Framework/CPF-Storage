package org.commonprovenance.framework.store.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.vanilla.QualifiedName;

import io.vavr.control.Either;

@DisplayName("Provenance JSON Utils Test")
public class ProvDocumentUtilsDeserializerMetaDocTest {
  private final String DOCUMENT_JSON = """
      {
        "prefix" : {
          "xsd" : "http://www.w3.org/2001/XMLSchema#",
          "cpm" : "https://www.commonprovenancemodel.org/cpm-namespace-v1-0/",
          "storage" : "http://localhost:8080/api/v1/documents/",
          "pav" : "http://purl.org/pav/",
          "prov" : "http://www.w3.org/ns/prov#",
          "meta" : "http://localhost:8080/api/v1/documents/meta/",
          "_": "https://openprovenance.org/blank#"
          },
        "bundle" : {
          "meta:ca76d97e-b329-4df5-8bc8-9765aaa532fd" : {
            "entity" : {
              "storage:3ebf557a-ca10-4451-a8f0-76fc1e5a6329" : {
                          "cpm:jwt":{
                          "$":"eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsInRydXN0ZWRQYXJ0eVVyaSI6ImxvY2FsaG9zdDo4MDIwIiwieDVjIjpbIk1JSUNNakNDQWRpZ0F3SUJBZ0lVU0xqNVk3UFhJUzEzcVBFUERkbElOQm5Rem9nd0NnWUlLb1pJemowRUF3SXdiVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnUkdWdGJ5QkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt4SWpBZ0JnTlZCQU1NR1VSUVJDQkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt3SGhjTk1qUXhNVEUyTURJMU9UVXlXaGNOTXpReE1URTBNREkxT1RVeVdqQmRNUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFWlcxdklGUnlkWE4wWldRZ1VHRnlkSGt4R2pBWUJnTlZCQU1NRVVSUVJDQlVjblZ6ZEdWa0lGQmhjblI1TUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFK1Y4a1Q0amt2RVdtWDMwMUtBUzlla2xtblJOaTZnVTkrS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBREFPQmdOVkhROEJBZjhFQkFNQ0FZWXdIUVlEVlIwT0JCWUVGTUNuUFJqaVhva1Q3cXV3WlJCMTZBQWd6N2JuTUI4R0ExVWRJd1FZTUJhQUZDeUVLd2kxanZkUHFmaVUrTmRIL252aDdQWVpNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxRlJBWllnSWdWaDRTMnRRbjEyWFlkZDVJU3NDcEFCc2g2WnJqU2lWWXJ0MlQxTzFuUXN3PSJdfQ.eyJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL3YxL29yZ2FuaXphdGlvbnMvNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjL2RvY3VtZW50cy9kYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiLCJoYXNoX2FsZyI6IlNIQTI1NiIsImRvY19kaWdlc3QiOiI0MWVhNWY5OWU1YjE5MzA5MWFhMTQ4MGY4ODMxNzdiM2M1MzU1ODM4M2E4NTE2Y2IyZGUxNmE0ZmRjOThiOWQyIiwib3JnX2lkIjoiNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjIiwiaXNzIjoiVHJ1c3RlZFBhcnR5IiwiaWF0IjoxNzg3MTU3OTAzLCJkb2NfaWF0IjoxNzg3MTU3OTAzfQ.na5rHFKL8MHH0-nfvNDvbW3qiJselLPTQrSs5xA8yyP0ailmaK1_AeYwQ5w7XNomE49MJ9lYEwnOJs-wFQ5iVA",
                          "type" : "xsd:base64Binary"
          },
                          "prov:type":{
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "cpm:Token"
                          }
                        },
                        "storage:c7ff3b2a-dca9-4ef3-b1dd-f66f2347607d" : {
                          "cpm:jwt":"eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsInRydXN0ZWRQYXJ0eVVyaSI6ImxvY2FsaG9zdDo4MDIwIiwieDVjIjpbIk1JSUNNakNDQWRpZ0F3SUJBZ0lVU0xqNVk3UFhJUzEzcVBFUERkbElOQm5Rem9nd0NnWUlLb1pJemowRUF3SXdiVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnUkdWdGJ5QkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt4SWpBZ0JnTlZCQU1NR1VSUVJDQkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt3SGhjTk1qUXhNVEUyTURJMU9UVXlXaGNOTXpReE1URTBNREkxT1RVeVdqQmRNUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFWlcxdklGUnlkWE4wWldRZ1VHRnlkSGt4R2pBWUJnTlZCQU1NRVVSUVJDQlVjblZ6ZEdWa0lGQmhjblI1TUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFK1Y4a1Q0amt2RVdtWDMwMUtBUzlla2xtblJOaTZnVTkrS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBREFPQmdOVkhROEJBZjhFQkFNQ0FZWXdIUVlEVlIwT0JCWUVGTUNuUFJqaVhva1Q3cXV3WlJCMTZBQWd6N2JuTUI4R0ExVWRJd1FZTUJhQUZDeUVLd2kxanZkUHFmaVUrTmRIL252aDdQWVpNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxRlJBWllnSWdWaDRTMnRRbjEyWFlkZDVJU3NDcEFCc2g2WnJqU2lWWXJ0MlQxTzFuUXN3PSJdfQ.eyJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL3YxL29yZ2FuaXphdGlvbnMvNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjL2RvY3VtZW50cy9iNGJlY2JjZS01ZWU0LTQ0MDgtODdlMy01YWRkNTViNzJjNWYiLCJoYXNoX2FsZyI6IlNIQTI1NiIsImRvY19kaWdlc3QiOiI4MzAzMDFhOGMwOGQ4MjI1MDM3NDg2YTFjYmY5NTIzYjE2NTEwMjM5ZmVlMTUwZTY4OWM1ZWE2YTA0OGEyYTQxIiwib3JnX2lkIjoiNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjIiwiaXNzIjoiVHJ1c3RlZFBhcnR5IiwiaWF0IjoxNzg3MTU3OTY0LCJkb2NfaWF0IjoxNzg3MTU3OTY0fQ.P10ys3Uy5RlRJvZ3MUXV2hhirYbwj7XKJWVsMQOhxYAuiLEChvMa7BQA5yq95wRIZYNAPAJdPUW-oqO5npFRgQ",
                          "prov:type" :  {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "cpm:Token"
                          }
                        },
                        "storage:b4becbce-5ee4-4408-87e3-5add55b72c5f" : {
                          "pav:version" :  {
                            "type" : "xsd:integer",
                            "$" : "2"
                          } ,
                          "prov:type" : {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "prov:Bundle"
                          }
                        },
                        "storage:dc8efed0-0035-4029-9065-8c46667151db" : {
                          "pav:version" : [ {
                            "type" : "xsd:integer",
                            "$" : "1"
                          } ],
                          "prov:type" : [ {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "prov:Bundle"
                          } ]
                        },
                        "storage:af634e47-3fd4-4f13-9f7a-8f5b7f5facbf" : {
                          "prov:type" : [ {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "prov:Bundle"
                          } ]
                        }
                      },
                      "activity" : {
                        "storage:9be5c016-c27b-4ef1-8ff5-f3f1da815e38" : {
                          "prov:startTime" : "2026-08-19T18:46:04.000+02:00",
                          "prov:endTime" : "2026-08-19T18:46:04.000+02:00",
                          "prov:type" : [ {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "cpm:TokenGeneration"
                          } ]
                        },
                        "storage:687558ea-8811-4801-9165-44ddf432d338" : {
                          "prov:startTime" : "2026-08-19T18:45:03.000+02:00",
                          "prov:endTime" : "2026-08-19T18:45:03.000+02:00",
                          "prov:type" : [ {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "cpm:TokenGeneration"
                          } ]
                        }
                      },
                      "agent" : {
                        "storage:TrustedParty" : {
                          "cpm:trustedPartyUri" : [ "localhost:8020" ],
                          "prov:type" : [ {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "cpm:TrustedParty"
                          } ],
                          "cpm:trustedPartyCertificate" : [ "MIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIwbTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2UgRGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBdMQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBEZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9+KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYDVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRjiXokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZMAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1FRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=" ]
                        }
                      },
                      "used" : {
                        "_:n18" : {
                          "prov:activity" : "storage:9be5c016-c27b-4ef1-8ff5-f3f1da815e38",
                          "prov:entity" : "storage:b4becbce-5ee4-4408-87e3-5add55b72c5f"
                        },
                        "_:n20" : {
                          "prov:activity" : "storage:687558ea-8811-4801-9165-44ddf432d338",
                          "prov:entity" : "storage:dc8efed0-0035-4029-9065-8c46667151db"
                        }
                      },
                      "wasAssociatedWith" : {
                        "_:n19" : {
                          "prov:activity" : "storage:9be5c016-c27b-4ef1-8ff5-f3f1da815e38",
                          "prov:agent" : "storage:TrustedParty"
                        },
                        "_:n21" : {
                          "prov:activity" : "storage:687558ea-8811-4801-9165-44ddf432d338",
                          "prov:agent" : "storage:TrustedParty"
                        }
                      },
                      "wasAttributedTo" : {
                        "_:n16" : {
                          "prov:entity" : "storage:3ebf557a-ca10-4451-a8f0-76fc1e5a6329",
                          "prov:agent" : "storage:TrustedParty"
                        },
                        "_:n13" : {
                          "prov:entity" : "storage:c7ff3b2a-dca9-4ef3-b1dd-f66f2347607d",
                          "prov:agent" : "storage:TrustedParty"
                        },
                        "_:n14" : {
                          "prov:entity" : "storage:c7ff3b2a-dca9-4ef3-b1dd-f66f2347607d",
                          "prov:agent" : "storage:9be5c016-c27b-4ef1-8ff5-f3f1da815e38"
                        },
                        "_:n17" : {
                          "prov:entity" : "storage:3ebf557a-ca10-4451-a8f0-76fc1e5a6329",
                          "prov:agent" : "storage:687558ea-8811-4801-9165-44ddf432d338"
                        }
                      },
                      "wasDerivedFrom" : {
                        "_:n11" : {
                          "prov:generatedEntity" : "storage:b4becbce-5ee4-4408-87e3-5add55b72c5f",
                          "prov:usedEntity" : "storage:dc8efed0-0035-4029-9065-8c46667151db",
                          "prov:type" : [ {
                            "type" : "prov:QUALIFIED_NAME",
                            "$" : "prov:Revision"
                          } ]
                        }
                      },
                      "qualifiedSpecializationOf" : {
                        "_:n15" : {
                          "prov:specificEntity" : "storage:dc8efed0-0035-4029-9065-8c46667151db",
                          "prov:generalEntity" : "storage:af634e47-3fd4-4f13-9f7a-8f5b7f5facbf"
                        },
                        "_:n12" : {
                          "prov:specificEntity" : "storage:b4becbce-5ee4-4408-87e3-5add55b72c5f",
                          "prov:generalEntity" : "storage:af634e47-3fd4-4f13-9f7a-8f5b7f5facbf"
                        }
                      }
                    }
                  }
                }""";;

  private Document provDoc;

  private void testInit() {
    Consumer<ApplicationException> onError = (e) -> fail(e.getMessage(), e.getCause());
    Consumer<Document> onSuccess = (docuemnt) -> {
      this.provDoc = docuemnt;
    };

    Either.<ApplicationException, String> right(this.DOCUMENT_JSON)
        .flatMap(ProvDocumentUtils.deserialize(Formats.ProvFormat.JSON))
        .peek(onSuccess::accept)
        .peekLeft(onError::accept);
  }

  @Test
  @DisplayName("should have exact 3 namespaces - Document - Deserializer")
  public void should_have_exact_3_namespaces() {
    this.testInit();

    Bundle b = (Bundle) this.provDoc.getStatementOrBundle().getFirst();
    assertEquals("ca76d97e-b329-4df5-8bc8-9765aaa532fd", b.getId().getLocalPart());
    assertEquals("http://localhost:8080/api/v1/documents/meta/", b.getId().getNamespaceURI());
    assertEquals("meta", b.getId().getPrefix());
    assertEquals("http://localhost:8080/api/v1/documents/meta/ca76d97e-b329-4df5-8bc8-9765aaa532fd", b.getId().getUri());

  }

  @Test
  @DisplayName("should have exact 3 namespaces - Document - Deserializer")
  public void should_have_exact_entities_namespaces() {
    this.testInit();

    Bundle b = (Bundle) this.provDoc.getStatementOrBundle().getFirst();

    List<Entity> entities = b.getStatement().stream()
        .filter(Entity.class::isInstance)
        .map(Entity.class::cast)
        .collect(Collectors.toList());

    assertEquals(5, entities.size());

    List<Entity> tokens = entities.stream()
        .filter(e -> e.getType().size() == 1 && ((QualifiedName) e.getType().getFirst().getValue()).getLocalPart().equals("Token"))
        .collect(Collectors.toList());

    assertEquals(2, tokens.size());

    List<Other> jwts = tokens.stream()
        .flatMap(t -> t.getOther().stream())
        .filter(o -> o.getElementName().getLocalPart().equals("jwt"))
        .map(o -> o)
        .collect(Collectors.toList());

    assertEquals(2, jwts.size());

    // Entity token = tokens.getFirst();

    // List<Other> jwts = token.getOther().stream()
    // .filter(o -> o.getElementName().getLocalPart().equals("jwt"))
    // .collect(Collectors.toList());

    // assertEquals(1, jwts.size());

    // Other jwt = jwts.getFirst();

    // assertInstanceOf(LangString.class, jwt.getValue());

  }
}

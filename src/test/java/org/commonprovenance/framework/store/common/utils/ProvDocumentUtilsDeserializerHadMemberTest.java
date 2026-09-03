package org.commonprovenance.framework.store.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.HadMember;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.interop.Formats;

import io.vavr.control.Either;

@DisplayName("Provenance JSON Utils Test")
public class ProvDocumentUtilsDeserializerHadMemberTest {
  private final String DOCUMENT_JSON_1 = """
      {
        "prefix": {
          "ex": "https://www.example.com/",
          "_": "https://openprovenance.org/blank#"
        },
        "bundle": {
          "ex:bundleA": {
            "entity": {
              "ex:e0": {},
              "ex:e1": {},
              "ex:e2": {},
              "ex:c": {
                "prov:type": {
                  "$": "prov:Collection",
                  "type": "xsd:QName"
                }
              }
            },
            "hadMember": {
              "_:hM1": {
                "prov:collection": "ex:c",
                "prov:entity": "ex:e0"
              },
              "_:hM2": {
                "prov:collection": "ex:c",
                "prov:entity": "ex:e1"
              },
              "_:hM3": {
                "prov:collection": "ex:c",
                "prov:entity": "ex:e2"
              }
            }
          }
        }
      }      """;;

  private final String DOCUMENT_JSON_2 = """
      {
        "prefix": {
          "ex": "https://www.example.com/",
          "_": "https://openprovenance.org/blank#"
        },
        "bundle": {
          "ex:bundleA": {
            "entity": {
              "ex:e0": {},
              "ex:e1": {},
              "ex:e2": {},
              "ex:c": {
                "prov:type": {
                  "$": "prov:Collection",
                  "type": "xsd:QName"
                }
              }
            },
            "hadMember": {
              "_:hM1": {
                "prov:collection": "ex:c",
                "prov:entity": ["ex:e0", "ex:e1", "ex:e2"]
              }
            }
          }
        }
      }      """;;

  private Document provDoc;

  private void testInit(String json) {
    Consumer<ApplicationException> onError = (e) -> fail(e.getMessage(), e.getCause());
    Consumer<Document> onSuccess = (docuemnt) -> {
      this.provDoc = docuemnt;
    };

    Either.<ApplicationException, String> right(json)
        .flatMap(ProvDocumentUtils.deserialize(Formats.ProvFormat.JSON))
        .peek(onSuccess::accept)
        .peekLeft(onError::accept);
  }

  @Test
  @DisplayName("should have exact bundleId - Document - Deserializer")
  public void should_have_exact_bundleId() {
    this.testInit(this.DOCUMENT_JSON_1);

    Bundle b = (Bundle) this.provDoc.getStatementOrBundle().getFirst();
    assertEquals("bundleA", b.getId().getLocalPart());
    assertEquals("https://www.example.com/", b.getId().getNamespaceURI());
    assertEquals("ex", b.getId().getPrefix());
    assertEquals("https://www.example.com/bundleA", b.getId().getUri());

  }

  @Test
  @DisplayName("should have exact collection entity - Document - Deserializer")
  public void should_have_exact_collection() {
    this.testInit(this.DOCUMENT_JSON_1);

    Bundle b = (Bundle) this.provDoc.getStatementOrBundle().getFirst();
    List<Entity> collection = b.getStatement().stream()
        .filter(Entity.class::isInstance)
        .map(Entity.class::cast)
        .filter(e -> e.getType().size() == 1
            && QualifiedName.class.isInstance(e.getType().getFirst().getValue())
            && ((QualifiedName) e.getType().getFirst().getValue()).getLocalPart().equals("Collection"))
        .collect(Collectors.toList());

    System.out.println(collection.size());
    collection.forEach(c -> System.out.print(c.getId()));
  }

  @Test
  @DisplayName("should have exact hadMember relations - Document - Deserializer")
  public void should_have_exact_hadMembers() {
    this.testInit(this.DOCUMENT_JSON_1);

    Bundle b = (Bundle) this.provDoc.getStatementOrBundle().getFirst();

    List<HadMember> hadMembers = b.getStatement().stream()
        .filter(HadMember.class::isInstance)
        .map(HadMember.class::cast)
        .collect(Collectors.toList());

    assertEquals(3, hadMembers.size());

    boolean allSameCollection = hadMembers.stream()
        .map(HadMember::getCollection)
        .distinct()
        .limit(2)
        .count() <= 1;

    assertTrue(allSameCollection, "should have same Collection entity");

    List<QualifiedName> entities = hadMembers.stream()
        .map(HadMember::getEntity)
        .flatMap(List::stream)
        .toList();
    assertEquals(
        Set.of("https://www.example.com/e2", "https://www.example.com/e0", "https://www.example.com/e1"),
        new HashSet<>(entities.stream().map(QualifiedName::getUri).toList()));
  }

  @Test
  @DisplayName("should have exact hadMember relation - Document - Deserializer")
  public void should_have_exact_hadMember() {
    this.testInit(this.DOCUMENT_JSON_2);

    Bundle b = (Bundle) this.provDoc.getStatementOrBundle().getFirst();

    List<HadMember> hadMembers = b.getStatement().stream()
        .filter(HadMember.class::isInstance)
        .map(HadMember.class::cast)
        .collect(Collectors.toList());

    assertEquals(1, hadMembers.size());

    boolean allSameCollection = hadMembers.stream()
        .map(HadMember::getCollection)
        .distinct()
        .limit(2)
        .count() <= 1;

    assertTrue(allSameCollection, "should have same Collection entity");

    List<QualifiedName> entities = hadMembers.stream()
        .map(HadMember::getEntity)
        .flatMap(List::stream)
        .toList();
    assertEquals(
        Set.of("https://www.example.com/e2", "https://www.example.com/e0", "https://www.example.com/e1"),
        new HashSet<>(entities.stream().map(QualifiedName::getUri).toList()));
  }
}

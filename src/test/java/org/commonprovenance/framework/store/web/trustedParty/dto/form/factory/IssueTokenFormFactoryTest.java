package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenFormDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("IssueTokenFormFactory UnitTest")
public class IssueTokenFormFactoryTest {
  private String SIGNATURE = "MEYCIQC9g58Sk51VWk82yMt9N2BDHnW/a+Hf7y3+RonC5/54YAIhAN1dHjSbyJHRXdKBRvudW7Ix+q71SDewwlLK2njHCdEe";
  private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7e";
  private String TIME_STAMP = "2026-11-20T11:04:33Z";
  private String GRAPH = "ewogICJidW5kbGUiOiB7CiAgICAic3RvcmFnZTpkYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiOiB7CiAgICAgICJwcmVmaXgiOiB7CiAgICAgICAgInNjaGVtYSI6ICJodHRwczovL3NjaGVtYS5vcmcvIiwKICAgICAgICAiY3BtIjogImh0dHBzOi8vd3d3LmNvbW1vbnByb3ZlbmFuY2Vtb2RlbC5vcmcvY3BtLW5hbWVzcGFjZS12MS0wLyIsCiAgICAgICAgImJsYW5rIjogImh0dHBzOi8vb3BlbnByb3ZlbmFuY2Uub3JnL2JsYW5rLyIsCiAgICAgICAgInhzZCI6ICJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSMiLAogICAgICAgICJvcmNpZCI6ICJodHRwczovL29yY2lkLm9yZy8iLAogICAgICAgICJvYm9saWJyYXJ5IjogImh0dHA6Ly9wdXJsLm9ib2xpYnJhcnkub3JnL29iby8iLAogIC..";

  private Clock clock = Clock.fixed(
      Instant.parse(TIME_STAMP),
      ZoneOffset.UTC);

  @Mock
  Organization organization;

  @Mock
  Document document;

  @Mock
  MetaDocument metaDocument;

  @Nested
  @DisplayName("build IssueTokenFormDTO")
  class BuildIssueTokenFormDTO {
    @Test
    @DisplayName("Build IssueTokenFormDTO from Organization, Document, GraphType, maybe signature and exact Clock")
    void build_buildForm() {
      when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);

      when(document.getGraph()).thenReturn(GRAPH);
      when(document.getFormat()).thenReturn(GraphFormat.JSON);

      IssueTokenFormDTO form = IssueTokenFormFactory.build(organization, document, GraphType.GRAPH, Optional.of(SIGNATURE), clock);
      assertEquals(ORGANIZATION_ID, form.organizationId());
      assertEquals(GRAPH, form.graph());
      assertEquals(GraphFormat.JSON, form.graphFormat());
      assertEquals(SIGNATURE, form.signature());
      assertEquals(GraphType.GRAPH, form.graphType());
      assertEquals(TIME_STAMP, form.createdOn());
    }
  }

  @Nested
  @DisplayName("build safe IssueTokenFormDTO with full graph and signature")
  class BuildIssueTokenFormDTOSafeGraph {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Build IssueTokenFormDTO from Organization and signature SAFE")
      void buildSafe_buildForm() {
        when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
        when(organization.getDocument()).thenReturn(Optional.of(document));

        when(document.getGraph()).thenReturn(GRAPH);
        when(document.getFormat()).thenReturn(GraphFormat.JSON);

        IssueTokenFormFactory.buildSafe(organization, SIGNATURE, clock)
            .peek(form -> {
              assertEquals(ORGANIZATION_ID, form.organizationId());
              assertEquals(GRAPH, form.graph());
              assertEquals(GraphFormat.JSON, form.graphFormat());
              assertEquals(SIGNATURE, form.signature());
              assertEquals(GraphType.GRAPH, form.graphType());
              assertEquals(TIME_STAMP, form.createdOn());
            })
            .peekLeft(e -> fail("Left side has not been expected, got: " + e.getMessage()));
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Organization is null")
      void buildSafe_failNullOrganization() {

        IssueTokenFormFactory.buildSafe(null, SIGNATURE)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build IssueToken form, because organization is null!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Document has not been set in Organization")
      void buildSafe_failEmptyDocument() {
        when(organization.getDocument()).thenReturn(Optional.empty());

        IssueTokenFormFactory.buildSafe(organization, SIGNATURE)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build IssueToken form, because Document is empty!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Form is not fully hydrated")
      void buildSafe_failNotHydrated() {
        when(organization.getDocument()).thenReturn(Optional.of(document));

        IssueTokenFormFactory.buildSafe(organization, "")
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(ConstraintException.class, e, "Should be ConstraintException");
              assertEquals(
                  "Validation of class 'IssueTokenFormDTO' faild with message:"
                      + " Field with name 'organizationId' can not be null!"
                      + ", Field with name 'graph' can not be null!"
                      + ", Field with name 'graphFormat' can not be null!"
                      + ", Field with name 'signature' can not be blank!",
                  e.getMessage(),
                  "Should have exact message");
            });
      }
    }
  }

  @Nested
  @DisplayName("build safe IssueTokenFormDTO with subgraph without signature")
  class BuildIssueTokenFormDTOSafeSubGraph {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Build IssueTokenFormDTO SAFE")
      void buildSafe_buildForm() {
        when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
        when(organization.getDocument()).thenReturn(Optional.of(document));

        when(document.getGraph()).thenReturn(GRAPH);
        when(document.getFormat()).thenReturn(GraphFormat.JSON);

        IssueTokenFormFactory.buildSafe(organization, GraphType.BACKBONE, clock)
            .peek(form -> {
              assertEquals(ORGANIZATION_ID, form.organizationId());
              assertEquals(GRAPH, form.graph());
              assertEquals(GraphFormat.JSON, form.graphFormat());
              assertEquals(null, form.signature());
              assertEquals(GraphType.BACKBONE, form.graphType());
              assertEquals(TIME_STAMP, form.createdOn());
            })
            .peekLeft(e -> fail("Left side has not been expected, got: " + e.getMessage()));
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Organization is null")
      void buildSafe_failNullOrganization() {

        IssueTokenFormFactory.buildSafe(null, GraphType.BACKBONE)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build IssueToken form, because organization is null!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Document has not been set in Organization")
      void buildSafe_failEmptyDocument() {
        when(organization.getDocument()).thenReturn(Optional.empty());

        IssueTokenFormFactory.buildSafe(organization, GraphType.BACKBONE)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build IssueToken form, because Document is empty!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Form is not fully hydrated")
      void buildSafe_failNotHydrated() {
        when(organization.getDocument()).thenReturn(Optional.of(document));

        IssueTokenFormFactory.buildSafe(organization, GraphType.BACKBONE)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(ConstraintException.class, e, "Should be ConstraintException");
              assertEquals(
                  "Validation of class 'IssueTokenFormDTO' faild with message:"
                      + " Field with name 'organizationId' can not be null!"
                      + ", Field with name 'graph' can not be null!"
                      + ", Field with name 'graphFormat' can not be null!",
                  e.getMessage(),
                  "Should have exact message");
            });
      }
    }
  }

  @Nested
  @DisplayName("build safe IssueTokenFormDTO with MetaDocument")
  class BuildIssueTokenFormDTOSafeMetaDocument {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Build IssueTokenFormDTO SAFE")
      void buildSafe_buildForm() {
        when(metaDocument.getOrganizationIdentifier()).thenReturn(Either.right(ORGANIZATION_ID));
        when(metaDocument.getB64Graph()).thenReturn(Either.right(GRAPH));

        IssueTokenFormFactory.buildSafe(metaDocument, clock)
            .peek(form -> {
              assertEquals(ORGANIZATION_ID, form.organizationId());
              assertEquals(GRAPH, form.graph());
              assertEquals(GraphFormat.JSON, form.graphFormat());
              assertEquals(null, form.signature());
              assertEquals(GraphType.META, form.graphType());
              assertEquals(TIME_STAMP, form.createdOn());
            })
            .peekLeft(e -> fail("Left side has not been expected, got: " + e.getMessage()));
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if MetaDocument is null")
      void buildSafe_failNullOrganization() {

        IssueTokenFormFactory.buildSafe(null)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build IssueToken form, because metaDocument is null!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Organization id can not be obtained")
      void buildSafe_failOrgId() {
        Either<ApplicationException, String> errorOrId = Either.left(new InvalidValueException("Some Exception!"));

        when(metaDocument.getOrganizationIdentifier()).thenReturn(errorOrId);

        IssueTokenFormFactory.buildSafe(metaDocument)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Some Exception!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if meta graph can not be obtained")
      void buildSafe_failGraph() {
        Either<ApplicationException, String> errorOrGraph = Either.left(new InvalidValueException("Some Exception!"));

        when(metaDocument.getOrganizationIdentifier()).thenReturn(Either.right(ORGANIZATION_ID));
        when(metaDocument.getB64Graph()).thenReturn(errorOrGraph);

        IssueTokenFormFactory.buildSafe(metaDocument)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Some Exception!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build IssueTokenFormDTO fail, if Form is not fully hydrated")
      void buildSafe_failNotHydrated() {
        when(metaDocument.getOrganizationIdentifier()).thenReturn(Either.right(null));
        when(metaDocument.getB64Graph()).thenReturn(Either.right(null));

        IssueTokenFormFactory.buildSafe(metaDocument)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(ConstraintException.class, e, "Should be ConstraintException");
              assertEquals(
                  "Validation of class 'IssueTokenFormDTO' faild with message:"
                      + " Field with name 'organizationId' can not be null!"
                      + ", Field with name 'graph' can not be null!",
                  e.getMessage(),
                  "Should have exact message");
            });
      }
    }
  }
}

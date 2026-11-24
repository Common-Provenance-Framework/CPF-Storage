package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.VerifySignatureFormDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifySignatureFormFactoryTest UnitTest")
public class VerifySignatureFormFactoryTest {
  private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7e";
  private String SIGNATURE = "MEYCIQC9g58Sk51VWk82yMt9N2BDHnW/a+Hf7y3+RonC5/54YAIhAN1dHjSbyJHRXdKBRvudW7Ix+q71SDewwlLK2njHCdEe";
  private String GRAPH = "ewogICJidW5kbGUiOiB7CiAgICAic3RvcmFnZTpkYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiOiB7CiAgICAgICJwcmVmaXgiOiB7CiAgICAgICAgInNjaGVtYSI6ICJodHRwczovL3NjaGVtYS5vcmcvIiwKICAgICAgICAiY3BtIjogImh0dHBzOi8vd3d3LmNvbW1vbnByb3ZlbmFuY2Vtb2RlbC5vcmcvY3BtLW5hbWVzcGFjZS12MS0wLyIsCiAgICAgICAgImJsYW5rIjogImh0dHBzOi8vb3BlbnByb3ZlbmFuY2Uub3JnL2JsYW5rLyIsCiAgICAgICAgInhzZCI6ICJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSMiLAogICAgICAgICJvcmNpZCI6ICJodHRwczovL29yY2lkLm9yZy8iLAogICAgICAgICJvYm9saWJyYXJ5IjogImh0dHA6Ly9wdXJsLm9ib2xpYnJhcnkub3JnL29iby8iLAogIC..";

  @Mock
  Organization organization;

  @Mock
  Document document;

  @Nested
  @DisplayName("build VerifySignatureFormDTO")
  class BuildVerifySignatureFormDTO {
    @Test
    @DisplayName("Build VerifySignatureFormDTO from Organization, Document and signature")
    void build_buildForm() {
      when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
      when(document.getGraph()).thenReturn(GRAPH);

      VerifySignatureFormDTO form = VerifySignatureFormFactory.build(organization, document, SIGNATURE);
      assertEquals(ORGANIZATION_ID, form.organizationId(), "should have exact id");
      assertEquals(GRAPH, form.graph(), "should have exact client cert");
      assertEquals(SIGNATURE, form.signature(), "should have exact signature");
    }
  }

  @Nested
  @DisplayName("build safe VerifySignatureFormDTO")
  class BuildSafeVerifySignatureFormDTO {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Build VerifySignatureFormDTO SAFE")
      void buildSafe_buildForm() {
        when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
        when(organization.getDocument()).thenReturn(Optional.of(document));
        when(document.getGraph()).thenReturn(GRAPH);

        VerifySignatureFormFactory.buildSafe(SIGNATURE)
            .apply(organization)
            .peek(form -> {
              assertEquals(ORGANIZATION_ID, form.organizationId(), "should have exact id");
              assertEquals(GRAPH, form.graph(), "should have exact client cert");
              assertEquals(SIGNATURE, form.signature(), "should have exact signature");
            })
            .peekLeft(e -> fail("Left side has not been expected, got: " + e.getMessage()));
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Build VerifySignatureFormDTO fail, if Organization is null")
      void buildSafe_failNullOrganization() {

        VerifySignatureFormFactory.buildSafe(SIGNATURE)
            .apply(null)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build VerifySignature form, because organization is null!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build VerifySignatureFormDTO fail, if Document has not been set in Organization")
      void buildSafe_failEmptyDocument() {
        when(organization.getDocument()).thenReturn(Optional.empty());

        VerifySignatureFormFactory.buildSafe(SIGNATURE)
            .apply(organization)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build VerifySignature form, because Document is empty!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build VerifySignatureFormDTO fail, if Form is not fully hydrated")
      void buildSafe_failNotHydrated() {

        when(organization.getDocument()).thenReturn(Optional.of(document));
        when(organization.getIdentifier()).thenReturn(null);
        when(document.getGraph()).thenReturn(null);

        VerifySignatureFormFactory.buildSafe("")
            .apply(organization)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(ConstraintException.class, e, "Should be ConstraintException");
              assertEquals(
                  "Validation of class 'VerifySignatureFormDTO' faild with message:"
                      + " Field with name 'organizationId' can not be null!"
                      + ", Field with name 'graph' can not be null!"
                      + ", Field with name 'signature' can not be blank!",
                  e.getMessage(),
                  "Should have exact message");
            });
      }
    }
  }
}

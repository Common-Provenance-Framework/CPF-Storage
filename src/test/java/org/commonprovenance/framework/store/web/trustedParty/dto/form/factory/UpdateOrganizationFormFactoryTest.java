package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.util.List;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationFormDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateOrganizationFormFactory UnitTest")
public class UpdateOrganizationFormFactoryTest {
  private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7e";

  private String ORGANIZATION_CERT = """
      -----BEGIN CERTIFICATE-----
      MIIB8DCCAZWgAwIBAgIUajAZAZAMKJqrnd/5kF9gBsWqZncwCgYIKoZIzj0EAwIw
      LjELMAkGA1UEBhMCQ1oxDDAKBgNVBAoMA0NQRjERMA8GA1UEAwwIY3BmLWludDIw
      HhcNMjYxMTE2MTIxMTI0WhcNMjkwMjE4MTIxMTI0WjBKMQswCQYDVQQGEwJDWjEM
      MAoGA1UECgwDQ1BGMS0wKwYDVQQDDCQ2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0w
      NzlhZDlkMDFlN2UwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAASD1L1cy4FP8NsI
      PBZC18iNZugiGZRRhO7ZgTzDyD0WOYucsiS6CUBtiIawhPosZXLOvtEkbCmvGdFD
      QjGdyjEmo3UwczAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDATBgNVHSUE
      DDAKBggrBgEFBQcDAjAdBgNVHQ4EFgQUrz/Q+NtvSPMnmskTD7KzxLLRoXkwHwYD
      VR0jBBgwFoAUWuNDU3VuQ3L53GQJN0wHo5snWNUwCgYIKoZIzj0EAwIDSQAwRgIh
      AIHFzhwQPGF0OtnrRkYXfDHtNz1KyY7wGwBbSViUeTHnAiEArN4DY/sV4y7u5std
      933tUOgAOm0oPQyh4UwrfwSvUlU=
      -----END CERTIFICATE-----
      """;

  private String INT1_CERT = """
      -----BEGIN CERTIFICATE-----
      MIICAzCCAamgAwIBAgIUXasg/hr17lvGO6wzCe9psqHFD9MwCgYIKoZIzj0EAwIw
      bTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug
      RGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp
      Y2F0ZSBBdXRob3JpdHkwHhcNMjYwMzMxMTAyNDM1WhcNMzEwMzMwMTAyNDM1WjAu
      MQswCQYDVQQGEwJDWjEMMAoGA1UECgwDQ1BGMREwDwYDVQQDDAhjcGYtaW50MTBZ
      MBMGByqGSM49AgEGCCqGSM49AwEHA0IABLmZq7t7VPs6bJyNnH6NUBAxvSCEdRRS
      QGq+ESZc5+zETASkOPfqHEQkc/atqz+d7X/Sf/aDn7JwZ0mnEC6hCpqjZjBkMBIG
      A1UdEwEB/wQIMAYBAf8CAQEwDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBRpLdHn
      PMpijgKLlLtiD+dzzS8GqTAfBgNVHSMEGDAWgBQshCsItY73T6n4lPjXR/574ez2
      GTAKBggqhkjOPQQDAgNIADBFAiAAo3t8i/BVbOZ68JKn/j+YTzRlFyohlj1nd6qc
      rokKFAIhAJK4xAJfPdQr89D1PzKh8GAJiGqbeJh0f4UY/bSIXcwl
      -----END CERTIFICATE-----
      """;

  private String INT2_CERT = """
      -----BEGIN CERTIFICATE-----
      MIIBxDCCAWqgAwIBAgIUTwRHh0gkw0wGo95BQ0erOuMtQ24wCgYIKoZIzj0EAwIw
      LjELMAkGA1UEBhMCQ1oxDDAKBgNVBAoMA0NQRjERMA8GA1UEAwwIY3BmLWludDEw
      HhcNMjYwMzMxMTAyNDQzWhcNMzEwMzMwMTAyNDQzWjAuMQswCQYDVQQGEwJDWjEM
      MAoGA1UECgwDQ1BGMREwDwYDVQQDDAhjcGYtaW50MjBZMBMGByqGSM49AgEGCCqG
      SM49AwEHA0IABA9JvLi5DwAZixixQ41Xnaqzg5HjAAl9X1JQot0wVvfG1cws3pX4
      3vW6fYfkmUON3MKlNmZxsUvkD6lD1+QqaF+jZjBkMBIGA1UdEwEB/wQIMAYBAf8C
      AQAwDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBRa40NTdW5DcvncZAk3TAejmydY
      1TAfBgNVHSMEGDAWgBRpLdHnPMpijgKLlLtiD+dzzS8GqTAKBggqhkjOPQQDAgNI
      ADBFAiBXucydJVT/fLBX5UwZPN2eZATmGK04wBgeSY0xuGC5SAIhAJjmBRNQm1lD
      pxKlNY7frNF+N5eoC9UA6cOqvRtJ2C5r
      -----END CERTIFICATE-----
      """;

  @Mock
  Organization organization;

  @Nested
  @DisplayName("build UpdateOrganizationFormDTO")
  class BuildUpdateOrganizationFormDTO {
    @Test
    @DisplayName("Build UpdateOrganizationFormDTO from Organization")
    void build_buildForm() {
      when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
      when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
      when(organization.getIntermediateCertificates()).thenReturn(List.of(
          INT1_CERT,
          INT2_CERT));

      UpdateOrganizationFormDTO form = UpdateOrganizationFormFactory.build(organization);
      assertEquals(ORGANIZATION_ID, form.id(), "should have exact id");
      assertEquals(ORGANIZATION_CERT, form.clientCertificate(), "should have exact client cert");
      assertEquals(List.of(INT1_CERT, INT2_CERT), form.intermediateCertificates(), "should have exact intermediate certs");
    }
  }

  @Nested
  @DisplayName("build safe UpdateOrganizationFormDTO")
  class BuildSafeUpdateOrganizationFormDTO {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Build UpdateOrganizationFormDTO SAFE")
      void buildSafe_buildForm() {
        when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(
            INT1_CERT,
            INT2_CERT));

        UpdateOrganizationFormFactory.buildSafe(organization)
            .peek(form -> {
              assertEquals(ORGANIZATION_ID, form.id());
              assertEquals(ORGANIZATION_CERT, form.clientCertificate());
              assertEquals(List.of(INT1_CERT, INT2_CERT), form.intermediateCertificates());
            })
            .peekLeft(e -> fail("Left side has not been expected, got: " + e.getMessage()));
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Build UpdateOrganizationFormDTO fail, if Organization is null")
      void buildSafe_failNullOrganization() {

        UpdateOrganizationFormFactory.buildSafe(null)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(InvalidValueException.class, e, "Should be InvalidValueException");
              assertEquals("Can not build UpdateOrganization form, because organization is null!", e.getMessage(), "Should have exact message");
            });
      }

      @Test
      @DisplayName("Build UpdateOrganizationFormDTO fail, if Form is not fully hydrated")
      void buildSafe_failNotHydrated() {
        when(organization.getIdentifier()).thenReturn(null);
        when(organization.getClientCertificate()).thenReturn("");
        when(organization.getIntermediateCertificates()).thenReturn(List.of());

        UpdateOrganizationFormFactory.buildSafe(organization)
            .peek(_ -> fail("Right side has not been expected!"))
            .peekLeft(e -> {
              assertInstanceOf(ApplicationException.class, e, "Should be ApplicationException");
              assertInstanceOf(ConstraintException.class, e, "Should be ConstraintException");
              assertEquals(
                  "Validation of class 'UpdateOrganizationFormDTO' faild with message:"
                      + " Field with name 'id' can not be null!"
                      + ", Field with name 'clientCertificate' can not be blank!",
                  e.getMessage(),
                  "Should have exact message");
            });
      }
    }
  }
}

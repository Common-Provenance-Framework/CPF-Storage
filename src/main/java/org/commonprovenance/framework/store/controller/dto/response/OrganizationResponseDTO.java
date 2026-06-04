package org.commonprovenance.framework.store.controller.dto.response;

import java.util.Collections;
import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OrganizationResponse", description = "Organization details")
public class OrganizationResponseDTO implements
    HasIdentifier<OrganizationResponseDTO>,
    HasClientCertificate<OrganizationResponseDTO>,
    HasIntermediateCertificates<OrganizationResponseDTO> {

  @Schema(description = "Organization identifier", example = "853226ba-9d56-4129-b51a-3b534f88957d")
  private final String identifier;

  @Schema(description = "PEM encoded client certificate", example = "-----BEGIN CERTIFICATE-----\n" +
      "MIIB8DCCAZWgAwIBAgIUbbA1CP+STZ240t1UH477j/tNMSQwCgYIKoZIzj0EAwIw\n" +
      "LjELMAkGA1UEBhMCQ1oxDDAKBgNVBAoMA0NQRjERMA8GA1UEAwwIY3BmLWludDIw\n" +
      "HhcNMjYwMzMxMTAyNDU1WhcNMjgwNzAzMTAyNDU1WjBKMQswCQYDVQQGEwJDWjEM\n" +
      "MAoGA1UECgwDQ1BGMS0wKwYDVQQDDCQ2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0w\n" +
      "NzlhZDlkMDFlN2MwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQ5ps+Ibs+yEoxc\n" +
      "rHRHiOTSblN0rD95161MgFNvf5rXbLTnjsOx9IsVVlKOGx9NH3+MV98D8Wm51YLI\n" +
      "wxUdjFVlo3UwczAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDATBgNVHSUE\n" +
      "DDAKBggrBgEFBQcDAjAdBgNVHQ4EFgQUEwUuDicDuxN8Nhx9tCPZkliiYUAwHwYD\n" +
      "VR0jBBgwFoAUWuNDU3VuQ3L53GQJN0wHo5snWNUwCgYIKoZIzj0EAwIDSQAwRgIh\n" +
      "AJieCv0bUDa4H8vVtSOYZXMP9GN6aTMt5uxwwnbc+3egAiEAwe/uUGbedlPNWNfR\n" +
      "Xo4J8wBdiiMmYXa9MGU7TWVuuoU=\n" +
      "-----END CERTIFICATE-----")
  private final String clientCertificate;

  @Schema(description = "PEM encoded intermediate certificates")
  private final List<String> intermediateCertificates;

  public OrganizationResponseDTO(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
  }

  public OrganizationResponseDTO() {
    this.identifier = null;
    this.clientCertificate = null;
    this.intermediateCertificates = Collections.emptyList();
  }

  @Override
  public OrganizationResponseDTO withIdentifier(String identifier) {
    return new OrganizationResponseDTO(
        identifier,
        this.getClientCertificate(),
        this.getIntermediateCertificates());
  }

  @Override
  public OrganizationResponseDTO withClientCertificate(String clientCertificate) {
    return new OrganizationResponseDTO(
        this.getIdentifier(),
        clientCertificate,
        this.getIntermediateCertificates());
  }

  @Override
  public OrganizationResponseDTO withIntermediateCertificates(List<String> intermediateCertificates) {
    return new OrganizationResponseDTO(
        this.getIdentifier(),
        this.getClientCertificate(),
        intermediateCertificates);
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

}

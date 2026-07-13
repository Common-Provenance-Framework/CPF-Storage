package org.commonprovenance.framework.store.controller.dto.response;

import java.util.List;

import org.commonprovenance.framework.store.common.dtos.HasClientCertificate;
import org.commonprovenance.framework.store.common.dtos.HasIdentifier;
import org.commonprovenance.framework.store.common.dtos.HasIntermediateCertificates;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OrganizationResponse", description = "Organization details")
public record OrganizationResponseDTO(
    @Schema(description = "Organization identifier", example = "853226ba-9d56-4129-b51a-3b534f88957d")

    String identifier,
    @Schema(description = "PEM encoded client certificate", example = "-----BEGIN CERTIFICATE-----" +
        "MIIB8DCCAZWgAwIBAgIUbbA1CP+STZ240t1UH477j/tNMSQwCgYIKoZIzj0EAwIw" +
        "LjELMAkGA1UEBhMCQ1oxDDAKBgNVBAoMA0NQRjERMA8GA1UEAwwIY3BmLWludDIw" +
        "HhcNMjYwMzMxMTAyNDU1WhcNMjgwNzAzMTAyNDU1WjBKMQswCQYDVQQGEwJDWjEM" +
        "MAoGA1UECgwDQ1BGMS0wKwYDVQQDDCQ2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0w" +
        "NzlhZDlkMDFlN2MwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQ5ps+Ibs+yEoxc" +
        "rHRHiOTSblN0rD95161MgFNvf5rXbLTnjsOx9IsVVlKOGx9NH3+MV98D8Wm51YLI" +
        "wxUdjFVlo3UwczAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDATBgNVHSUE" +
        "DDAKBggrBgEFBQcDAjAdBgNVHQ4EFgQUEwUuDicDuxN8Nhx9tCPZkliiYUAwHwYD" +
        "VR0jBBgwFoAUWuNDU3VuQ3L53GQJN0wHo5snWNUwCgYIKoZIzj0EAwIDSQAwRgIh" +
        "AJieCv0bUDa4H8vVtSOYZXMP9GN6aTMt5uxwwnbc+3egAiEAwe/uUGbedlPNWNfR" +
        "Xo4J8wBdiiMmYXa9MGU7TWVuuoU=" +
        "-----END CERTIFICATE-----")

    String clientCertificate,
    @Schema(description = "PEM encoded intermediate certificates", example = "[\"-----BEGIN CERTIFICATE-----"
        +
        "MIICAzCCAamgAwIBAgIUXasg/hr17lvGO6wzCe9psqHFD9MwCgYIKoZIzj0EAwIw" +
        "bTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug" +
        "RGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp" +
        "Y2F0ZSBBdXRob3JpdHkwHhcNMjYwMzMxMTAyNDM1WhcNMzEwMzMwMTAyNDM1WjAu" +
        "MQswCQYDVQQGEwJDWjEMMAoGA1UECgwDQ1BGMREwDwYDVQQDDAhjcGYtaW50MTBZ" +
        "MBMGByqGSM49AgEGCCqGSM49AwEHA0IABLmZq7t7VPs6bJyNnH6NUBAxvSCEdRRS" +
        "QGq+ESZc5+zETASkOPfqHEQkc/atqz+d7X/Sf/aDn7JwZ0mnEC6hCpqjZjBkMBIG" +
        "A1UdEwEB/wQIMAYBAf8CAQEwDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBRpLdHn" +
        "PMpijgKLlLtiD+dzzS8GqTAfBgNVHSMEGDAWgBQshCsItY73T6n4lPjXR/574ez2" +
        "GTAKBggqhkjOPQQDAgNIADBFAiAAo3t8i/BVbOZ68JKn/j+YTzRlFyohlj1nd6qc" +
        "rokKFAIhAJK4xAJfPdQr89D1PzKh8GAJiGqbeJh0f4UY/bSIXcwl" +
        "-----END CERTIFICATE-----" +
        "\",\"-----BEGIN CERTIFICATE-----" +
        "MIIBxDCCAWqgAwIBAgIUTwRHh0gkw0wGo95BQ0erOuMtQ24wCgYIKoZIzj0EAwIw" +
        "LjELMAkGA1UEBhMCQ1oxDDAKBgNVBAoMA0NQRjERMA8GA1UEAwwIY3BmLWludDEw" +
        "HhcNMjYwMzMxMTAyNDQzWhcNMzEwMzMwMTAyNDQzWjAuMQswCQYDVQQGEwJDWjEM" +
        "MAoGA1UECgwDQ1BGMREwDwYDVQQDDAhjcGYtaW50MjBZMBMGByqGSM49AgEGCCqG" +
        "SM49AwEHA0IABA9JvLi5DwAZixixQ41Xnaqzg5HjAAl9X1JQot0wVvfG1cws3pX4" +
        "3vW6fYfkmUON3MKlNmZxsUvkD6lD1+QqaF+jZjBkMBIGA1UdEwEB/wQIMAYBAf8C" +
        "AQAwDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBRa40NTdW5DcvncZAk3TAejmydY" +
        "1TAfBgNVHSMEGDAWgBRpLdHnPMpijgKLlLtiD+dzzS8GqTAKBggqhkjOPQQDAgNI" +
        "ADBFAiBXucydJVT/fLBX5UwZPN2eZATmGK04wBgeSY0xuGC5SAIhAJjmBRNQm1lD" +
        "pxKlNY7frNF+N5eoC9UA6cOqvRtJ2C5r" +
        "-----END CERTIFICATE-----" +
        "\"]")

    List<String> intermediateCertificates) implements
    HasIdentifier,
    HasClientCertificate,
    HasIntermediateCertificates {
  public OrganizationResponseDTO {
    intermediateCertificates = intermediateCertificates == null ? List.of() : List.copyOf(intermediateCertificates);
  }
}

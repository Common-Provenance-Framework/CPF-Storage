package org.commonprovenance.framework.store.support.fixture;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasJwtToken.JwtHeaderItems;
import org.commonprovenance.framework.store.common.dto.HasJwtToken.JwtPayloadItems;
import org.commonprovenance.framework.store.support.WireMockInitializer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class TrustedPartyFixture {

  public static final String NAME = "default";
  public static final String CERTIFICATE = "trusted-party-certificate";
  public static final String ISSUER = "TrustedParty";
  public static final String HASH_ALGORITHM = "SHA256";

  private TrustedPartyFixture() {
  }

  public static String url() {
    return WireMockInitializer.trustedPartyUrl();
  }

  public static String issuedTokenFor(ProvDocumentFixture document) {
    Instant issuedAt = Instant.now();
    SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(JOSEObjectType.JWT)
            .customParam(JwtHeaderItems.TRUSTED_PARTY_URI.getLabel(), url())
            .x509CertChain(List.of(Base64.encode(CERTIFICATE)))
            .build(),
        new JWTClaimsSet.Builder()
            .subject(document.bundleUrl())
            .issuer(ISSUER)
            .issueTime(Date.from(issuedAt))
            .claim(JwtPayloadItems.HASH_ALGORITHM.getLabel(), HASH_ALGORITHM)
            .claim(JwtPayloadItems.DOCUMENT_DIGEST.getLabel(), document.digest())
            .claim(JwtPayloadItems.ORGANIZATION_ID.getLabel(), document.organizationId())
            .claim(JwtPayloadItems.DOCUMENT_TIMESTAMP.getLabel(), issuedAt.getEpochSecond())
            .build());
    try {
      jwt.sign(new ECDSASigner(new ECKeyGenerator(Curve.P_256).generate()));
    } catch (JOSEException e) {
      throw new IllegalStateException("Can not sign TrustedParty token", e);
    }
    return jwt.serialize();
  }
}

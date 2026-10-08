package org.commonprovenance.framework.store.support.fixture;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasJwtToken.JwtHeaderItems;
import org.commonprovenance.framework.store.common.dto.HasJwtToken.JwtPayloadItems;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

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
  public static final String URL = "http://localhost:8093/api/v1";
  public static final String CERTIFICATE = "trusted-party-certificate";
  public static final String ISSUER = "TrustedParty";
  public static final String HASH_ALGORITHM = "SHA256";

  private TrustedPartyFixture() {
  }

  public static TrustedParty defaultTrustedParty() {
    return new TrustedParty(NAME, CERTIFICATE, URL, true, true, true);
  }

  public static Token issueToken(ProvDocumentFixture document) {
    Instant issuedAt = Instant.now();
    return new Token(signedJwt(document, issuedAt), null, issuedAt.getEpochSecond());
  }

  private static String signedJwt(ProvDocumentFixture document, Instant issuedAt) {
    SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(JOSEObjectType.JWT)
            .customParam(JwtHeaderItems.TRUSTED_PARTY_URI.getLabel(), URL)
            .x509CertChain(List.of(Base64.encode(CERTIFICATE)))
            .build(),
        new JWTClaimsSet.Builder()
            .subject(document.bundleUrl())
            .issuer(ISSUER)
            .issueTime(Date.from(issuedAt))
            .claim(JwtPayloadItems.HASH_ALGORITHM.getLabel(), HASH_ALGORITHM)
            .claim(JwtPayloadItems.DOCUMENT_DIGEST.getLabel(), sha256(document.json()))
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

  private static String sha256(String value) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}

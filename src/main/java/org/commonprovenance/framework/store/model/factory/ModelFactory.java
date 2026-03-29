package org.commonprovenance.framework.store.model.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.commonprovenance.framework.store.common.dto.HasDocumentFormat;
import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.common.utils.Validators;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.exceptions.ArgumentValidatorException;
import org.commonprovenance.framework.store.model.AdditionalData;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.Trusts;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.CertificateTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyTPResponseDTO;

import reactor.core.publisher.Mono;

public class ModelFactory {
  private static <T extends HasId> Mono<String> getId(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasId::getId);
  }

  private static <T extends HasDocumentFormat> Mono<Format> getDocumentFormat(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasDocumentFormat::getDocumentFormat)
        .flatMap(MONO.<String>makeSureNotNullWithMessage("DTO 'format' can not be null."))
        .map(Format::from)
        .flatMap(MONO.<Optional<Format>>makeSure(
            Optional::isPresent,
            "Format '" + dto.getDocumentFormat() + "' is not valid Document format."))
        .map(Optional::get);
  }

  private static <T extends HasFormat> Mono<Format> getFormat(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasFormat::getFormat)
        .flatMap(MONO.<String>makeSureNotNullWithMessage("DTO 'format' can not be null."))
        .map(Format::from)
        .flatMap(MONO.<Optional<Format>>makeSure(
            Optional::isPresent,
            "Format '" + dto.getFormat() + "' is not valid Document format."))
        .map(Optional::get);
  }

  private static Document fromDto(DocumentFormDTO dto) {
    return new Document(
        null,
        dto.getOrganizationIdentifier(),
        dto.getDocument(),
        null,
        dto.getSignature());
  }

  private static Document fromDto(DocumentTPResponseDTO dto) {
    return new Document(
        null,
        null,
        dto.getDocument(),
        null,
        dto.getSignature());
  }

  private static Document fromPersistance(DocumentNode document) {
    return new Document(
        document.getIdentifier(),
        null,
        document.getGraph(),
        null,
        null);
  }

  private static TrustedParty fromPersistance(TrustedPartyNode trustedParty) {
    return new TrustedParty(
        trustedParty.getName(),
        trustedParty.getClientCertificate(),
        trustedParty.getUrl(),
        trustedParty.getIsChecked(),
        trustedParty.getIsValid(),
        trustedParty.getIsDefault())
        .withId(trustedParty.getId());
  }

  private static Organization fromPersistance(OrganizationNode organization) {
    Organization org = new Organization(
        organization.getIdentifier(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());

    return organization.getTrusts().stream()
        .map(Trusts::getTrustedParty)
        .map(ModelFactory::fromPersistance)
        .findFirst()
        .map(org::withTrustedParty)
        .orElse(org);
  }

  private static Token fromPersistance(TokenNode token) {

    Function<TokenNode, AdditionalData> additionalDataFactory = (TokenNode node) -> new AdditionalData(
        node.getBundle(),
        node.getOrganizationIdentifier(),
        node.getHashFunction(),
        node.getTrustedPartyUri(),
        node.getTrustedPartyCertificate(),
        node.getMessageTimestamp());

    return new Token(
        null,
        token.getHash(),
        token.getSignature(),
        additionalDataFactory.apply(token),
        ModelFactory.toDomain(token.getWasIssuedBy().getFirst().getTrustedParty()).block(),
        ModelFactory.toDomain(token.getBelongsTo().getFirst().getDocument()).block(),
        token.getTokenTimestamp());
  }

  private static Organization fromDto(OrganizationTPResponseDTO dto) {
    return new Organization(
        dto.getId(),
        dto.getCertificate(),
        Collections.emptyList());
  }

  private static Organization fromDto(CertificateTPResponseDTO dto) {
    return new Organization(
        dto.getId(),
        dto.getCertificate(),
        Collections.emptyList());
  }

  private static Organization fromDto(OrganizationFormDTO dto) {
    return new Organization(
        dto.getIdentifier(),
        dto.getClientCertificate(),
        dto.getIntermediateCertificates());
  }

  private static Token fromDto(TokenTPResponseDTO dto) {
    AdditionalData additionalData = new AdditionalData(
        dto.getData().getAdditionalData().getBundle(),
        dto.getData().getOriginatorId(),
        dto.getData().getAdditionalData().getHashFunction(),
        dto.getData().getAdditionalData().getTrustedPartyUri(),
        dto.getData().getAdditionalData().getTrustedPartyCertificate(),
        dto.getData().getDocumentCreationTimestamp());
    return new Token(
        null,
        dto.getData().getDocumentDigest(),
        dto.getSignature(),
        additionalData,
        null,
        null,
        dto.getData().getDocumentCreationTimestamp());
  }

  private static TrustedParty fromDto(TrustedPartyTPResponseDTO dto) {
    return new TrustedParty(
        dto.getId(),
        dto.getCertificate());
  }

  // ---
  // Trusted Party
  public static Mono<Organization> toDomain(OrganizationTPResponseDTO dto) {
    return MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto);
  }

  public static Mono<Organization> toDomain(CertificateTPResponseDTO dto) {
    return MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto);
  }

  public static Mono<Document> toDomain(DocumentTPResponseDTO dto) {
    return MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto);
  }

  public static Mono<Token> toDomain(TokenTPResponseDTO dto) {
    return MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto);
  }

  public static Function<TrustedPartyTPResponseDTO, Mono<TrustedParty>> toDomain(String url, Boolean isDefault) {
    return (TrustedPartyTPResponseDTO dto) -> MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto)
        .map((TrustedParty trustedParty) -> trustedParty.withUrl(url))
        .map((TrustedParty trustedParty) -> trustedParty.withIsDefault(isDefault));
  }

  public static Mono<TrustedParty> toDomain(TrustedPartyTPResponseDTO dto) {
    return MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto)
        .flatMap((TrustedParty trustedParty) -> ModelFactory.getId(dto).map(trustedParty::withId));
  }

  // Persistence
  public static Mono<Document> toDomain(DocumentNode entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance)
        .flatMap((Document document) -> ModelFactory.getFormat(entity).map(document::withFormat));
  }

  public static Mono<Organization> toDomain(OrganizationNode entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance);
  }

  public static Mono<TrustedParty> toDomain(TrustedPartyNode entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance)
        .flatMap((TrustedParty trustedParty) -> ModelFactory.getId(entity).map(trustedParty::withId));
  }

  public static Mono<Token> toDomain(TokenNode entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance)
        .flatMap((Token token) -> ModelFactory.getId(entity).map(token::withId));
  }

  // Controller
  public static Mono<Document> toDomain(DocumentFormDTO formDTO) {
    return MONO.makeSureNotNull(formDTO)
        .map(ModelFactory::fromDto)
        .flatMap((Document document) -> ModelFactory.getDocumentFormat(formDTO).map(document::withFormat));
  }

  public static Mono<Organization> toDomain(OrganizationFormDTO formDTO) {
    return MONO.makeSureNotNull(formDTO)
        .map(ModelFactory::fromDto);
  }

  public static Mono<UUID> toUUID(String uuid) {
    return MONO.<String>makeSureNotNullWithMessage("DTO 'id' can not be null.").apply(uuid)
        .flatMap(MONO.<String>makeSure(
            Validators::isUUID,
            (String id) -> new ArgumentValidatorException("Id '" + id + "' is not valid UUID string.")))
        .map(UUID::fromString)
        .onErrorResume(IllegalArgumentException.class,
            MONO.<IllegalArgumentException, UUID>exceptionWrapper(e -> "Can not parse uuid: " + e.getMessage()))
        .onErrorResume(MONO.<Throwable, UUID>exceptionWrapper());
  }
}

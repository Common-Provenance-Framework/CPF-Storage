package org.commonprovenance.framework.store.model.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.commonprovenance.framework.store.common.dto.HasDocumentFormat;
import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasHashFunction;
import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.utils.Validators;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.exceptions.ArgumentValidatorException;
import org.commonprovenance.framework.store.model.AdditionalData;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.HashFunction;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;
import org.commonprovenance.framework.store.persistence.entity.TokenEntity;
import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.CertificateTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyTPResponseDTO;
import tools.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

public class ModelFactory {
  private static <T extends HasId> Mono<UUID> getId(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasId::getId)
        .flatMap(ModelFactory::toUUID);
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

  private static <T extends HasOrganizationId<T>> Mono<UUID> getOrganizationId(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasOrganizationId<T>::getOrganizationId)
        .flatMap(ModelFactory::toUUID);
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

  private static <T extends HasHashFunction> Mono<HashFunction> getHashFunction(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasHashFunction::getHashFunction)
        .flatMap(MONO.<String>makeSureNotNullWithMessage("DTO 'hashFunction' can not be null."))
        .map(HashFunction::from)
        .flatMap(MONO.<Optional<HashFunction>>makeSure(
            Optional::isPresent,
            "HashFunction '" + dto.getHashFunction() + "' is not valid hash function."))
        .map(Optional::get);
  }

  // private static <T extends HasCreatedOn> Mono<Long> getCreated(T dto) {
  // return MONO.makeSureNotNull(dto)
  // .map(HasCreatedOn::getCreatedOn)
  // .flatMap(MONO.<Long>makeSureNotNullWithMessage("DTO 'createdOn' can not be
  // null."))
  // // .flatMap(MONO.<String>makeSure(
  // // Validators::isISO8601DateTime,
  // // (String created) -> "String '" + created + "' is not valid ISO8601
  // DateTime
  // // string"))
  // // .map(ZonedDateTime::parse)
  // .onErrorResume(IllegalArgumentException.class,
  // MONO.<IllegalArgumentException, Long>exceptionWrapper(
  // e -> "Can not parse date: " + e.getMessage()))
  // .onErrorResume(MONO.<Throwable, Long>exceptionWrapper());
  // }

  // ---
  private static Document fromDto(DocumentFormDTO dto) {
    return new Document(null, null, null, dto.getDocument(), null, dto.getSignature());
  }

  private static Document fromDto(DocumentTPResponseDTO dto) {
    return new Document(null, null, null, dto.getDocument(), null, dto.getSignature());
  }

  private static Document fromPersistance(DocumentEntity document) {
    return new Document(
        null,
        null,
        null,
        document.getGraph(),
        null,
        null);
  }

  private static TrustedParty fromPersistance(TrustedPartyEntity trustedParty) {
    return new TrustedParty(
        null,
        trustedParty.getName(),
        trustedParty.getClientCertificate(),
        trustedParty.getUrl(),
        trustedParty.getChecked(),
        trustedParty.getValid(),
        trustedParty.getIsDefault());
  }

  private static Organization fromPersistance(OrganizationEntity organization) {
    return new Organization(
        null,
        organization.getName(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates(),
        ModelFactory.toDomain(organization.getTrusts().getFirst().getTrustedParty()).block());
  }

  private static Token fromPersistance(TokenEntity token) {

    return new Token(
        null,
        token.getHash(),
        token.getSignature(),
        new ObjectMapper().readValue(token.getAdditionalData(), AdditionalData.class),
        ModelFactory.toDomain(token.getWasIssuedBy().getFirst().getTrustedParty()).block(),
        ModelFactory.toDomain(token.getBelongsTo().getFirst().getDocument()).block(),
        token.getTokenTimestamp());
  }

  private static Organization fromDto(OrganizationTPResponseDTO dto) {
    return new Organization(
        null,
        dto.getId(),
        dto.getCertificate(),
        Collections.emptyList(),
        null);
  }

  private static Organization fromDto(CertificateTPResponseDTO dto) {
    return new Organization(
        null,
        dto.getId(),
        dto.getCertificate(),
        Collections.emptyList(),
        null);
  }

  private static Organization fromDto(OrganizationFormDTO dto) {
    return new Organization(
        null,
        dto.getName(),
        dto.getClientCertificate(),
        dto.getIntermediateCertificates(),
        null);
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
  public static Mono<Document> toDomain(DocumentEntity entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance)
        .flatMap((Document document) -> ModelFactory.getId(entity).map(document::withId))
        .flatMap((Document document) -> ModelFactory.getFormat(entity).map(document::withFormat));

  }

  public static Mono<Organization> toDomain(OrganizationEntity entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance)
        .flatMap((Organization organization) -> ModelFactory.getId(entity).map(organization::withId));
  }

  public static Mono<TrustedParty> toDomain(TrustedPartyEntity entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance)
        .flatMap((TrustedParty trustedParty) -> ModelFactory.getId(entity).map(trustedParty::withId));
  }

  public static Mono<Token> toDomain(TokenEntity entity) {
    return MONO.makeSureNotNull(entity)
        .map(ModelFactory::fromPersistance)
        .flatMap((Token token) -> ModelFactory.getId(entity).map(token::withId));
  }

  // Controller
  public static Mono<Document> toDomain(DocumentFormDTO formDTO) {
    return MONO.makeSureNotNull(formDTO)
        .map(ModelFactory::fromDto)
        .flatMap((Document document) -> ModelFactory.getOrganizationId(formDTO).map(document::withOrganizationId))
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

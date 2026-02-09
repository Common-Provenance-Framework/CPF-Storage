package org.commonprovenance.framework.store.model.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.commonprovenance.framework.store.common.dto.HasCreated;
import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasHashFunction;
import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.common.utils.Validators;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.HashFunction;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyTPResponseDTO;

import reactor.core.publisher.Mono;

public class ModelFactory {
  private static <T extends HasId> Mono<UUID> getId(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasId::getId)
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

  private static <T extends HasCreated> Mono<ZonedDateTime> getCreated(T dto) {
    return MONO.makeSureNotNull(dto)
        .map(HasCreated::getCreated)
        .flatMap(MONO.<String>makeSureNotNullWithMessage("DTO 'created' can not be null."))
        .flatMap(MONO.<String>makeSure(
            Validators::isISO8601DateTime,
            (String created) -> "String '" + created + "' is not valid ISO8601 DateTime string"))
        .map(ZonedDateTime::parse)
        .onErrorResume(IllegalArgumentException.class,
            MONO.<IllegalArgumentException, ZonedDateTime>exceptionWrapper(
                e -> "Can not parse date: " + e.getMessage()))
        .onErrorResume(MONO.<Throwable, ZonedDateTime>exceptionWrapper());
  }

  // ---
  private static Document fromDto(DocumentFormDTO dto) {
    return new Document(null, dto.getGraph(), null);
  }

  private static Document fromDto(DocumentTPResponseDTO dto) {
    return new Document(null, dto.getGraph(), null);
  }

  private static Document fromPersistance(DocumentEntity document) {
    return new Document(null, document.getGraph(), null);
  }

  private static Organization fromPersistance(OrganizationEntity organization) {
    return new Organization(null,
        organization.getName(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  private static Organization fromDto(OrganizationTPResponseDTO dto) {
    return new Organization(
        null,
        dto.getName(),
        dto.getClientCertificate(),
        dto.getIntermediateCertificates());
  }

  private static Organization fromDto(OrganizationFormDTO dto) {
    return new Organization(
        null,
        dto.getName(),
        dto.getClientCertificate(),
        dto.getIntermediateCertificates());
  }

  private static Token fromDto(TokenTPResponseDTO dto) {
    return new Token(
        null,
        null,
        dto.getHash(),
        null,
        dto.getSignature(),
        null);
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
        .map(ModelFactory::fromDto)
        .flatMap((Organization organization) -> ModelFactory.getId(dto).map(organization::withId));
  }

  public static Mono<Document> toDomain(DocumentTPResponseDTO dto) {
    return MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto)
        .flatMap((Document document) -> ModelFactory.getId(dto).map(document::withId))
        .flatMap((Document document) -> ModelFactory.getFormat(dto).map(document::withFormat));
  }

  public static Mono<Token> toDomain(TokenTPResponseDTO dto) {
    return MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto)
        .flatMap((Token token) -> ModelFactory.getId(dto).map(token::withId))
        .flatMap((Token token) -> ModelFactory.toDomain(dto.getDocument()).map(token::withDocument))
        .flatMap((Token token) -> ModelFactory.getHashFunction(dto).map(token::withHashFunction))
        .flatMap((Token token) -> ModelFactory.getCreated(dto).map(token::withCreated));
  }

  public static Function<TrustedPartyTPResponseDTO, Mono<TrustedParty>> toDomain(String url) {
    return (TrustedPartyTPResponseDTO dto) -> MONO.makeSureNotNull(dto)
        .map(ModelFactory::fromDto)
        .flatMap((TrustedParty trustedParty) -> ModelFactory.getId(dto).map(trustedParty::withId))
        .map((TrustedParty trustedParty) -> trustedParty.withUrl(url));
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

  // Controller
  public static Mono<Document> toDomain(DocumentFormDTO formDTO) {
    return MONO.makeSureNotNull(formDTO)
        .map(ModelFactory::fromDto)
        .map((Document document) -> document.withId(UUID.randomUUID()))
        .flatMap((Document document) -> ModelFactory.getFormat(formDTO).map(document::withFormat));
  }

  public static Mono<Organization> toDomain(OrganizationFormDTO formDTO) {
    return MONO.makeSureNotNull(formDTO)
        .map(ModelFactory::fromDto);
  }

  public static Mono<UUID> toUUID(String uuid) {
    return MONO.<String>makeSureNotNullWithMessage("DTO 'id' can not be null.").apply(uuid)
        .flatMap(MONO.<String>makeSure(
            Validators::isUUID,
            (String id) -> "Id '" + id + "' is not valid UUID string."))
        .map(UUID::fromString)
        .onErrorResume(IllegalArgumentException.class,
            MONO.<IllegalArgumentException, UUID>exceptionWrapper(e -> "Can not parse uuid: " + e.getMessage()))
        .onErrorResume(MONO.<Throwable, UUID>exceptionWrapper());
  }
}

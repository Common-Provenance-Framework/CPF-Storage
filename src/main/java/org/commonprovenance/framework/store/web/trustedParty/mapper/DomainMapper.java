package org.commonprovenance.framework.store.web.trustedParty.mapper;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.commonprovenance.framework.store.common.utils.Validators;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.HashFunction;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.HasFormat;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.HasHashFunction;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.HasId;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.HasCreated;

import reactor.core.publisher.Mono;
import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

public class DomainMapper {
  private static <T extends HasId> Mono<UUID> getId(T dto) {
    return Mono.just(dto)
        .flatMap(MONO::<T>makeSureNotNull)
        .map(HasId::getId)
        .flatMap(MONO.<String>makeSureNotNullWithMessage("DTO 'id' can not be null."))
        .flatMap(MONO.<String>makeSure(
            Validators::isUUID,
            (String id) -> "Id '" + id + "' is not valid UUID string"))
        .map(UUID::fromString)
        .onErrorResume(IllegalArgumentException.class,
            MONO.<IllegalArgumentException, UUID>exceptionWrapper(e -> "Can not parse uuid: " + e.getMessage()))
        .onErrorResume(MONO.<Throwable, UUID>exceptionWrapper());
  }

  private static <T extends HasFormat> Mono<Format> getFormat(T dto) {
    return Mono.just(dto)
        .flatMap(MONO::<T>makeSureNotNull)
        .map(HasFormat::getFormat)
        .flatMap(MONO.<String>makeSureNotNullWithMessage("DTO 'format' can not be null."))
        .map(Format::from)
        .flatMap(MONO.<Optional<Format>>makeSure(
            Optional::isPresent,
            "Format '" + dto.getFormat() + "' is not valid Document format."))
        .map(Optional::get);
  }

  private static <T extends HasHashFunction> Mono<HashFunction> getHashFunction(T dto) {
    return Mono.just(dto)
        .flatMap(MONO::<T>makeSureNotNull)
        .map(HasHashFunction::getHashFunction)
        .flatMap(MONO.<String>makeSureNotNullWithMessage("DTO 'hashFunction' can not be null."))
        .map(HashFunction::from)
        .flatMap(MONO.<Optional<HashFunction>>makeSure(
            Optional::isPresent,
            "HashFunction '" + dto.getHashFunction() + "' is not valid hash function."))
        .map(Optional::get);
  }

  private static <T extends HasCreated> Mono<ZonedDateTime> getCreated(T dto) {
    return Mono.just(dto)
        .flatMap(MONO::<T>makeSureNotNull)
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

  public static Mono<Organization> toDomain(OrganizationResponseDTO dto) {
    return Mono.just(dto)
        .flatMap(MONO::<OrganizationResponseDTO>makeSureNotNull)
        .map(Organization::fromDto)
        .flatMap((Organization organization) -> DomainMapper.getId(dto).map(organization::withId));
  }

  public static Mono<Document> toDomain(DocumentResponseDTO dto) {
    return Mono.just(dto)
        .flatMap(MONO::<DocumentResponseDTO>makeSureNotNull)
        .map(Document::fromDto)
        .flatMap((Document document) -> DomainMapper.getId(dto).map(document::withId))
        .flatMap((Document document) -> DomainMapper.getFormat(dto).map(document::withFormat));
  }

  public static Mono<Token> toDomain(TokenResponseDTO dto) {
    return Mono.just(dto)
        .flatMap(MONO::<TokenResponseDTO>makeSureNotNull)
        .map(Token::fromDto)
        .flatMap((Token token) -> DomainMapper.getId(dto).map(token::withId))
        .flatMap((Token token) -> DomainMapper.toDomain(dto.getDocument()).map(token::withDocument))
        .flatMap((Token token) -> DomainMapper.getHashFunction(dto).map(token::withHashFunction))
        .flatMap((Token token) -> DomainMapper.getCreated(dto).map(token::withCreated));
  }
}

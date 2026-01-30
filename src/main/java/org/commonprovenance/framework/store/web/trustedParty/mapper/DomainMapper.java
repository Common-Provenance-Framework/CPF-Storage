package org.commonprovenance.framework.store.web.trustedParty.mapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.common.utils.Validators;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
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

public class DomainMapper {
  private static <T> Mono<T> makeSureNotNull(T value) {
    return DomainMapper.makeSure(Objects::nonNull, value, "DTO can not be null.");
  }

  private static <T> Mono<T> makeSureNotNull(T value, String message) {
    return DomainMapper.makeSure(Objects::nonNull, value, message);
  }

  private static <T> Mono<T> makeSure(Predicate<T> validator, T value, String message) {
    return validator.test(value)
        ? Mono.just(value)
        : Mono.error(new InternalApplicationException(message, new IllegalArgumentException()));
  }

  private static <T extends HasId> Mono<UUID> getId(T dto) {
    return DomainMapper.makeSureNotNull(dto)
        .flatMap((T model) -> DomainMapper.makeSureNotNull(model.getId(), "DTO 'id' can not be null."))
        .flatMap(
            (String id) -> DomainMapper.makeSure(Validators::isUUID, id, "id '" + id + "' is not valid UUID string"))
        .map(UUID::fromString)
        .onErrorResume(IllegalArgumentException.class,
            (IllegalArgumentException e) -> Mono
                .error(new InternalApplicationException("Can not parse uuid: " + e.getMessage(), e)))
        .onErrorResume(e -> Mono.error(new InternalApplicationException("Unexpected exception!", e)));
  }

  private static <T extends HasFormat> Mono<Format> getFormat(T dto) {
    return DomainMapper.makeSureNotNull(dto)
        .flatMap((T model) -> DomainMapper.makeSureNotNull(model.getFormat(), "DTO 'format' can not be null."))
        .map(Format::from)
        .flatMap(optFormat -> DomainMapper.makeSure(
            Optional::isPresent,
            optFormat,
            "Format '" + dto.getFormat() + "' is not valid Document format."))
        .map(Optional::get);
  }

  private static <T extends HasHashFunction> Mono<HashFunction> getHashFunction(T dto) {
    return DomainMapper.makeSureNotNull(dto)
        .flatMap(
            (T model) -> DomainMapper.makeSureNotNull(model.getHashFunction(), "DTO 'hashFunction' can not be null."))
        .map(HashFunction::from)
        .flatMap(optHashFunction -> DomainMapper.makeSure(
            Optional::isPresent,
            optHashFunction,
            "HashFunction '" + dto.getHashFunction() + "' is not valid hash function."))
        .map(Optional::get);
  }

  private static <T extends HasCreated> Mono<ZonedDateTime> getCreated(T dto) {
    return DomainMapper.makeSureNotNull(dto)
        .flatMap((T model) -> DomainMapper.makeSureNotNull(model.getCreated(), "DTO 'created' can not be null."))
        .flatMap((String created) -> DomainMapper.makeSure(Validators::isISO8601DateTime, created,
            "String '" + created + "' is not valid ISO8601 DateTime string"))
        .map(ZonedDateTime::parse)
        .onErrorResume(DateTimeParseException.class,
            (DateTimeParseException e) -> Mono
                .error(new InternalApplicationException("Can not parse date: " + e.getMessage(), e)))
        .onErrorResume((Throwable e) -> Mono.error(new InternalApplicationException("Unexpected exception!", e)));
  }

  // ---

  public static Mono<Organization> toDomain(OrganizationResponseDTO dto) {
    return DomainMapper.makeSureNotNull(dto)
        .map(Organization::fromDto)
        .flatMap((Organization organization) -> DomainMapper.getId(dto).map(organization::withId));
  }

  public static Mono<Document> toDomain(DocumentResponseDTO dto) {
    return DomainMapper.makeSureNotNull(dto)
        .map(Document::fromDto)
        .flatMap((Document document) -> DomainMapper.getId(dto).map(document::withId))
        .flatMap((Document document) -> DomainMapper.getFormat(dto).map(document::withFormat));
  }

  public static Mono<Token> toDomain(TokenResponseDTO dto) {
    return DomainMapper.makeSureNotNull(dto)
        .map(Token::fromDto)
        .flatMap((Token token) -> DomainMapper.getId(dto).map(token::withId))
        .flatMap((Token token) -> DomainMapper.toDomain(dto.getDocument()).map(token::withDocument))
        .flatMap((Token token) -> DomainMapper.getHashFunction(dto).map(token::withHashFunction))
        .flatMap((Token token) -> DomainMapper.getCreated(dto).map(token::withCreated));
  }
}

package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.TokenResponseFactory;

public interface HasTokenResponse<T extends HasTokenResponse<T>> {
  TokenResponseDTO getToken();

  T withToken(TokenResponseDTO token);

  static <T extends HasTokenResponse<T>, F extends HasTokenResponse<F>> UnaryOperator<T> addToken(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getToken)
        .map(to::withToken)
        .orElse(to);
  }

  static <T extends HasTokenResponse<T>, F extends HasTokenOptional<F>> UnaryOperator<T> addToken(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(F::getToken)
        .map(TokenResponseFactory::build)
        .map(to::withToken)
        .orElse(to);
  }

}

package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.common.dto.HasDocumentGraph;
import org.commonprovenance.framework.store.common.dto.HasDocumentOptional;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.VerifySignatureTPFormDTO;

import io.vavr.control.Either;

public class VerifySignatureFormFactory {
  private static <T extends HasIdentifier<T> & HasDocumentOptional<T>> UnaryOperator<VerifySignatureTPFormDTO> mapper(T data) {
    return MonoidComposition.<VerifySignatureTPFormDTO> composeOperators(
        List.of(
            HasOrganizationId.addOrganizationId(data),
            HasDocumentGraph.addDocument(data.getDocument())));
  }

  public static Function<Organization, Either<ApplicationException, VerifySignatureTPFormDTO>> build(String signature) {
    return (Organization organization) -> Either.<ApplicationException, VerifySignatureTPFormDTO> right(new VerifySignatureTPFormDTO())
        .map(VerifySignatureFormFactory.mapper(organization))
        .map(form -> form.withSignature(signature))
        .flatMap(EITHER::validateDTO);
  }
}

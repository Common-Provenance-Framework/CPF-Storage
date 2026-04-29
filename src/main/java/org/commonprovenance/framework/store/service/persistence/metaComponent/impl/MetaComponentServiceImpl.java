package org.commonprovenance.framework.store.service.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;
import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.utils.DocumentUtils;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class MetaComponentServiceImpl implements MetaComponentService {

  private final BundlePersistence bundlePersistence;

  public MetaComponentServiceImpl(
      BundlePersistence bundlePersistence) {
    this.bundlePersistence = bundlePersistence;
  }

  @Override
  public Mono<Void> createMetaComponent(Document document) {
    return Mono.just(document)
        .flatMap(MONO.makeSureNotNullWithMessage("Document can not be null!"))
        .flatMap(MONO.liftEffectToMono(DocumentUtils::getMainActivityReferenceMetaBundleId))
        .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' can not be null!"))
        .map(QualifiedName::getLocalPart)
        .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' local part can not be null!"))
        .flatMap(this.bundlePersistence::create);
  }

  @Override
  public Mono<Void> addNewVersion(Document document) {
    return Mono.just(document)
        .flatMap(MONO.makeSureNotNullWithMessage("Document can not be null!"))
        .flatMap(MONO.liftEffectToMono(DocumentUtils::getMainActivityReferenceMetaBundleId))
        .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' can not be null!"))
        .map(QualifiedName::getLocalPart)
        .flatMap(bundleIdentifier -> MONO.fromEither(DocumentUtils.getDocumentIdentifier(document))
            .flatMap(this.bundlePersistence.addVersionEntity(bundleIdentifier)));
  }

  @Override
  public Mono<Void> addTokenToLastVersion(Document document) {
    return Mono.just(document)
        .flatMap(MONO.makeSureNotNullWithMessage("Document can not be null!"))
        .flatMap(MONO.liftEffectToMono(DocumentUtils::getMainActivityReferenceMetaBundleId))
        .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' can not be null!"))
        .map(QualifiedName::getLocalPart)
        .flatMap((String bundleIdentifier) -> Mono.just(document)
            .flatMap(MONO.liftEffectToMono(EITHER.<Document, Token> liftEitherOptional(Document::getToken)))
            .flatMap(MONO.makeSureNotNullWithMessage("Token can not be null!"))
            .map(Token::getJwt)
            .flatMap(MONO.makeSureNotNullWithMessage("JWT Token can not be null!"))
            .flatMap(this.bundlePersistence.addToken(bundleIdentifier)));
  }

  @Override
  public Mono<Boolean> exists(String id) {
    return this.bundlePersistence.exists(id);
  }

}

package org.commonprovenance.framework.store.service.persistence.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.metaComponent.EntityRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.MetaBundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.NodeToProvFactory;
import org.commonprovenance.framework.store.service.persistence.MetaProvenanceComponentService;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class MetaProvenanceComponentServiceImpl implements MetaProvenanceComponentService {

  private final AppConfiguration configuration;

  private final MetaBundleRepository metaBundleRepository;
  private final EntityRepository entityRepository;

  public MetaProvenanceComponentServiceImpl(
      AppConfiguration configuration,
      MetaBundleRepository metaBundleRepository,
      EntityRepository entityRepository) {
    this.configuration = configuration;

    this.metaBundleRepository = metaBundleRepository;
    this.entityRepository = entityRepository;
  }

  @Override
  public Mono<Void> createMetaProvenanceComponentIfNotExists(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be null!")
        .apply(organization)
        .flatMap(MONO.liftOptionalToMono(
            Organization::getDocument,
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .flatMap(MONO.liftEffectToMono(Document::getMainActivity))
        .flatMap(MONO.liftEffectToMono(ProvDocumentUtils::getCpmReferencedMetaBundleId))
        .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' can not be null!"))
        .map(QualifiedName::getLocalPart)
        .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' local part can not be null!"))
        .flatMap(MONO.makeSureNotBefore(
            this.metaBundleRepository::existsByIdentifier,
            this.metaBundleRepository::create));
  }

  @Override
  public Mono<Void> addBundleVersionIntoMetaProvenanceComponent(Organization organization) {
    return MONO.combineM(
        Mono.just(organization)
            .flatMap(MONO.liftOptionalToMono(
                Organization::getDocument,
                _ -> new InvalidValueException("Document has not been deserialized yet!")))
            .flatMap(MONO.liftEffectToMono(Document::getMainActivity))
            .flatMap(MONO.liftEffectToMono(ProvDocumentUtils::getCpmReferencedMetaBundleId))
            .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' can not be null!"))
            .map(QualifiedName::getLocalPart),
        Mono.just(organization)
            .flatMap(MONO.liftOptionalToMono(
                Organization::getDocument,
                _ -> new InvalidValueException("Document has not been deserialized yet!")))
            .flatMap(MONO.liftEffectToMono(Document::getIdentifier))
            .flatMap(MONO.makeSureNotNullWithMessage("Bundle identifier can not be null!")),
        (metaBundleIdentifier, bundleIdentifier) -> metaBundleRepository.getLastVersionNo(metaBundleIdentifier)
            .defaultIfEmpty(0)
            .map(lastVersionNo -> lastVersionNo + 1)
            .flatMap(entityRepository.createBundleVersionEntity(metaBundleIdentifier, bundleIdentifier))
            .delayUntil(entityRepository.makeSpecializationOfGeneralVersion(metaBundleIdentifier))
            .delayUntil(entityRepository.makeRevisionOfPreviousVersion(metaBundleIdentifier))
            .delayUntil(metaBundleRepository.addEntityToMetaBundle(metaBundleIdentifier)))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Bundle version has not been added into meta provenance component!")));
  }

  @Override
  public Mono<Void> addTokenIntoMetaProvenanceComponent(Organization organization) {
    return MONO.combineM(
        Mono.just(organization)
            .flatMap(MONO.liftOptionalToMono(
                Organization::getDocument,
                _ -> new InvalidValueException("Document has not been deserialized yet!")))
            .flatMap(MONO.liftEffectToMono(Document::getMainActivity))
            .flatMap(MONO.liftEffectToMono(ProvDocumentUtils::getCpmReferencedMetaBundleId))
            .flatMap(MONO.makeSureNotNullWithMessage("'referenceMetaBundleId' can not be null!"))
            .map(QualifiedName::getLocalPart),
        Mono.just(organization)
            .flatMap(MONO.liftOptionalToMono(
                Organization::getDocument,
                _ -> new InvalidValueException("Document has not been deserialized yet!")))
            .flatMap(MONO.liftOptionalToMono(
                Document::getToken,
                "Token can not be null!")),
        (metaBundleIdentifier, token) -> entityRepository.createBundleTokenEntity(metaBundleIdentifier, token)
            .delayUntil(entityRepository.addToBundleVersionEntity(token))
            .delayUntil(metaBundleRepository.addEntityToMetaBundle(metaBundleIdentifier))
            .delayUntil(metaBundleRepository.addTokenGenerationToMetaBundle(metaBundleIdentifier))
            .delayUntil(metaBundleRepository.addTokenGeneratorToMetaBundle(metaBundleIdentifier)))
        .then();
  }

  @Override
  public Mono<Boolean> metaProvenanceComponentExists(String metaBundleIdentifier) {
    return MONO.<String> makeSureNotNullWithMessage("")
        .apply(metaBundleIdentifier)
        .flatMap(metaBundleRepository::existsByIdentifier);
  }

  @Override
  public Mono<org.openprovenance.prov.model.Document> getMetaProvenanceComponent(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(MONO.makeSureNotNullWithMessage("Meta provenance component identifier can not be null!"))
        .flatMap(metaBundleRepository::findByIdentifier)
        .flatMap(NodeToProvFactory.bundleToProv(configuration));
  }

}

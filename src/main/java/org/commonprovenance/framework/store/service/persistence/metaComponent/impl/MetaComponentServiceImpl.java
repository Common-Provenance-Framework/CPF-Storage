package org.commonprovenance.framework.store.service.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.EntityPersistence;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.springframework.stereotype.Service;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MetaComponentServiceImpl implements MetaComponentService {
  private final AppConfiguration configuration;
  private final ProvFactory provFactory;

  private final BundlePersistence bundlePersistence;
  private final EntityPersistence entityPersistence;

  public MetaComponentServiceImpl(
      AppConfiguration configuration,
      ProvFactory provFactory,
      BundlePersistence bundlePersistence,
      EntityPersistence entityPersistence) {
    this.configuration = configuration;
    this.provFactory = provFactory;
    this.bundlePersistence = bundlePersistence;
    this.entityPersistence = entityPersistence;
  }

  @Override
  public @NotNull Mono<Document> storeMetaComponent(@NotNull Document document) {
    return this.bundlePersistence.create(document);
  }

  @Override
  public @NotNull Mono<Document> createMetaComponent(@NotNull QualifiedName metaBundleId) {
    Entity generalEntity = this.provFactory.newEntity(
        this.provFactory.newQualifiedName(
            this.configuration.getFqdn() + "documents/",
            UUID.randomUUID().toString(),
            "storage"),
        List.of(provFactory.newType(
            provFactory.getName().PROV_BUNDLE,
            provFactory.getName().PROV_TYPE)));

    Namespace bundleNs = provFactory.newNamespace();
    bundleNs.register("meta", this.configuration.getFqdn() + "documents/meta/");

    Bundle bundle = provFactory.newNamedBundle(metaBundleId, bundleNs, List.of(generalEntity));

    Document provDocument = this.provFactory.newDocument();
    provDocument.getNamespace().addKnownNamespaces();
    provDocument.getNamespace().register(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
    provDocument.getNamespace().register("pav", "http://purl.org/pav/");
    provDocument.getNamespace().register("meta", this.configuration.getFqdn() + "documents/meta/");
    provDocument.getNamespace().register("storage", this.configuration.getFqdn() + "documents/");
    provDocument.getStatementOrBundle().add(bundle);

    return this.storeMetaComponent(provDocument);

  }

  @Override
  public @NotNull Function<Document, Mono<Document>> addNewVersion(QualifiedName identifier) {
    return (Document document) -> {
      Mono<Entity> generalEntity = this.getGeneralVersionEntity(document);
      Mono<Entity> lastVersionEntity = this.getLastVersionEntity(document);

      Mono<Entity> newVersionEntity = lastVersionEntity
          .map(getVersion(document.getNamespace().getNamespaces()))
          .flatMap(Mono::justOrEmpty)
          .switchIfEmpty(Mono.just(0))
          .map(this::incrementVersion)
          .map(this.createNewVersion(document.getNamespace().getNamespaces(), identifier));

      return Mono.zip(newVersionEntity, lastVersionEntity, generalEntity)
          .delayUntil(tuple -> {
            if (tuple.getT2().getId().equals(tuple.getT3().getId())) {
              // if new version is 1, last version is general
              return entityPersistence.addFirstVersion(tuple.getT3()).apply(tuple.getT1());
            }
            return Mono.just(null);
          })
          .map(tuple -> {
            List<Statement> statements = new ArrayList<>();
            statements.add(tuple.getT1());
            statements.add(provFactory.newSpecializationOf(tuple.getT1().getId(),
                tuple.getT3().getId()));

            if (!tuple.getT2().getId().equals(tuple.getT3().getId())) {
              // if new version is 1, last version is general
              WasDerivedFrom wdf = provFactory.newWasDerivedFrom(tuple.getT1().getId(),
                  tuple.getT2().getId());
              wdf.getType().add(provFactory.newType(
                  provFactory.getName().PROV_REVISION,
                  provFactory.getName().PROV_TYPE));
              statements.add(wdf);
            }
            return statements;
          })
          .flatMap(this.addStatementsToBundle(document));
    };

  }

  @Override
  public @NotNull Mono<Document> getMetaComponent(@NotNull QualifiedName metaBundleId) {
    return Mono.justOrEmpty(metaBundleId)
        .flatMap(this::getById)
        .onErrorResume(NotFoundException.class,
            _ -> this.createMetaComponent(metaBundleId));
  }

  @Override
  public @NotNull Mono<Document> getById(@NotNull QualifiedName id) {
    return this.bundlePersistence.getById(id.getLocalPart());
  }

  @Override
  public @NotNull Mono<Boolean> exists(@NotNull String id) {
    return this.bundlePersistence.exists(id);
  }

  private Function<Integer, Entity> createNewVersion(Map<String, String> namespaces, QualifiedName identifier) {
    return (Integer metaVersion) -> {
      // Map<String, String> namespaces = document.getNamespace().getNamespaces();

      Entity version = provFactory.newEntity(identifier);
      version.getType().add(provFactory.newType(
          provFactory.getName().PROV_BUNDLE,
          provFactory.getName().PROV_TYPE));

      version.getOther().add(provFactory.newOther(
          provFactory.newQualifiedName(namespaces.get("pav"), "version", "pav"),
          metaVersion,
          provFactory.getName().XSD_INT));
      return version;
    };
  }

  private Function<List<Statement>, Mono<Document>> addStatementsToBundle(Document document) {
    return (List<Statement> statements) -> Mono.justOrEmpty(document)
        .flatMap(doc -> getBundle(doc)
            .map(bundle -> provFactory.newNamedBundle(bundle.getId(), bundle.getNamespace(), Collections.emptyList()))
            .doOnNext(bundle -> statements.forEach(bundle.getStatement()::add))
            .map(bundle -> provFactory.newDocument(doc.getNamespace(), List.of(bundle))));
  }

  private Integer incrementVersion(Integer version) {
    return version + 1;
  }

  private QualifiedName getIdentifier(Map<String, String> namespaces, String local, String prefix) {
    return provFactory.newQualifiedName(namespaces.get(prefix), local, prefix);
  }

  private Flux<Entity> getBundleEntities(Document document) {
    return getBundle(document)
        .map(Bundle::getStatement)
        .flatMapMany(Flux::fromIterable)
        .filter(Entity.class::isInstance)
        .map(Entity.class::cast)
        .filter(this.isTypeOf(provFactory.getName().PROV_BUNDLE));
  }

  private Mono<Entity> getLastVersionEntity(Document document) {
    return getBundleEntities(document)
        .reduce((current, candidate) -> {
          Integer currentVersion = getVersion(document.getNamespace().getNamespaces())
              .apply(current).orElse(0);
          Integer candidateVersion = getVersion(document.getNamespace().getNamespaces())
              .apply(candidate).orElse(0);
          return candidateVersion > currentVersion ? candidate : current;
        });
  }

  private Mono<Entity> getGeneralVersionEntity(Document document) {
    return getBundleEntities(document)
        .filter(entity -> getVersion(document.getNamespace().getNamespaces())
            .apply(entity)
            .isEmpty())
        .next();
  }

  private Mono<Integer> getLastVersion(Document document) {
    return getBundleEntities(document)
        .map(this.getVersion(document.getNamespace().getNamespaces()))
        .flatMap(Mono::justOrEmpty)
        .sort()
        .last()
        .defaultIfEmpty(0);
  }

  private Function<Entity, Optional<Integer>> getVersion(Map<String, String> namespaces) {
    return (Entity entity) -> {
      List<Integer> versions = entity.getOther().stream()
          .filter(other -> other.getElementName().equals(provFactory.newQualifiedName(
              namespaces.get("pav"),
              "version",
              "pav")))
          .map(Other::getValue)
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .map(Integer::valueOf)
          .toList();
      return versions.size() == 1
          ? Optional.of(versions.getFirst())
          : Optional.empty();

    };
  }

  private Predicate<Entity> hasAttribure(QualifiedName attrName) {
    return (Entity entity) -> !entity.getOther().stream()
        .map(Other::getElementName)
        .filter(attrName::equals)
        .toList()
        .isEmpty();
  }

  private Predicate<Entity> isTypeOf(QualifiedName type) {
    return (Entity entity) -> !entity.getType().stream()
        .map(Type::getValue)
        .filter(QualifiedName.class::isInstance)
        .map(QualifiedName.class::cast)
        .filter(type::equals)
        .toList()
        .isEmpty();
  }

  private Mono<Bundle> getBundle(Document document) {
    return Flux.fromIterable(document.getStatementOrBundle())
        .filter(Bundle.class::isInstance)
        .map(Bundle.class::cast)
        .single()
        .onErrorResume(MONO.exceptionWrapper("MetaComponentService - Error while getting Bundle"));

  }

}
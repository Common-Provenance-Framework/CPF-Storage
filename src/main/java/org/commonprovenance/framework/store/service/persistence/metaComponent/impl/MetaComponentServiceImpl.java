package org.commonprovenance.framework.store.service.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.EntityPersistence;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
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
import org.openprovenance.prov.vanilla.LangString;
import org.openprovenance.prov.vanilla.ProvUtilities;
import org.springframework.stereotype.Service;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MetaComponentServiceImpl implements MetaComponentService {
  private final AppConfiguration configuration;

  private final ProvFactory provFactory;
  private final ICpmProvFactory cpmProvFactory;

  private final BundlePersistence bundlePersistence;
  private final EntityPersistence entityPersistence;

  public MetaComponentServiceImpl(
      AppConfiguration configuration,
      ProvFactory provFactory,
      ICpmProvFactory cpmProvFactory,
      BundlePersistence bundlePersistence,
      EntityPersistence entityPersistence) {
    this.configuration = configuration;
    this.provFactory = provFactory;
    this.cpmProvFactory = cpmProvFactory;
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
      Mono<Entity> generalEntity = this.getGeneralVersionEntity(document).cache();
      Mono<Entity> lastVersionEntity = this.getLastVersionEntity(document).cache();

      Mono<Entity> newVersionEntity = lastVersionEntity
          .map(getVersion(document.getNamespace().getNamespaces()))
          .flatMap(Mono::justOrEmpty)
          .switchIfEmpty(Mono.just(0))
          .map(this::incrementVersion)
          .map(this.createNewVersion(document.getNamespace().getNamespaces(), identifier))
          .cache();

      return Mono.zip(newVersionEntity, lastVersionEntity, generalEntity)
          .delayUntil(tuple -> {
            Entity newVersion = tuple.getT1();
            Entity lastVersion = tuple.getT2();
            Entity general = tuple.getT3();

            if (lastVersion.getId().equals(general.getId())) {
              // if new version is 1, last version is general
              return entityPersistence.addFirstVersion(general).apply(newVersion);
            } else {
              return entityPersistence.addNewVersion(general, lastVersion).apply(newVersion);
            }
          })
          .map(tuple -> {
            List<Statement> statements = new ArrayList<>();
            statements.add(tuple.getT1());
            statements.add(provFactory.newSpecializationOf(tuple.getT1().getId(),
                tuple.getT3().getId()));

            if (!tuple.getT2().getId().equals(tuple.getT3().getId())) {
              // if new version is 1, last version is general
              WasDerivedFrom wdf = provFactory.newWasDerivedFrom(
                  tuple.getT1().getId(),
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
  public @NotNull Function<Document, Mono<Document>> addTokenToLastVersion(Token tokenModel) {
    return (Document document) -> {
      Mono<Entity> lastVersionEntity = this.getLastVersionEntity(document)
          .cache(); // TODO: Mono is lazy and cold!!! Need to be refactored!!!

      Mono<Entity> tokenNode = lastVersionEntity
          .map(Entity::getId)
          .map(generateIdentifierFrom(UUID.randomUUID().toString()))
          .flatMap(this.createNewToken(tokenModel))
          .cache();

      Mono<Activity> activityNode = lastVersionEntity
          .map(Entity::getId)
          .map(generateIdentifierFrom(UUID.randomUUID().toString()))
          .flatMap(this.createNewTokenGeneration(tokenModel))
          .cache();

      Mono<Agent> agentNode = Mono.justOrEmpty(tokenModel)
          .map(Token::getTrustedParty)
          .map(TrustedParty::getId)
          .flatMap(Mono::justOrEmpty)
          .flatMap(uuid -> Mono.zip(Mono.just(uuid), lastVersionEntity.map(Entity::getId)))
          .map(tuple -> provFactory.newQualifiedName(
              tuple.getT2().getNamespaceURI(),
              tuple.getT1().toString(),
              tuple.getT2().getPrefix()))
          .flatMap(this.createNewTokenGenerator(tokenModel))
          .cache();

      return Mono.zip(lastVersionEntity, tokenNode, activityNode, agentNode)
          .delayUntil(tuple -> {
            Entity version = tuple.getT1();
            Entity token = tuple.getT2();
            Activity generation = tuple.getT3();
            Agent genrator = tuple.getT4();

            return entityPersistence.addToken(token, generation, genrator).apply(version);
          })
          .map(tuple -> {
            List<Statement> statements = new ArrayList<>();

            Entity version = tuple.getT1();
            Entity token = tuple.getT2();
            Activity generation = tuple.getT3();
            Agent generator = tuple.getT4();

            statements.add(token);
            statements.add(provFactory.newWasDerivedFrom(token.getId(), version.getId()));
            statements.add(provFactory.newWasGeneratedBy(null, token.getId(), generation.getId()));
            statements.add(provFactory.newWasAttributedTo(null, token.getId(), generator.getId()));

            statements.add(generation);
            statements.add(provFactory.newUsed(generation.getId(), version.getId()));
            statements.add(provFactory.newWasAssociatedWith(null, generation.getId(), generator.getId()));

            statements.add(generator);

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

  private Function<QualifiedName, Mono<Agent>> createNewTokenGenerator(Token tokenModel) {
    return (QualifiedName identifier) -> Mono.justOrEmpty(identifier)
        .map(provFactory::newAgent)
        .doOnNext(a -> {
          a.getType().add(provFactory.newType(
              cpmProvFactory.newCpmQualifiedName("trustedParty"),
              provFactory.getName().PROV_TYPE));

          a.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("trustedPartyUri"),
              tokenModel.getAdditionalData().getTrustedPartyUri(),
              provFactory.getName().XSD_STRING));

          a.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("trustedPartyCertificate"),
              tokenModel.getAdditionalData().getTrustedPartyCertificate(),
              provFactory.getName().XSD_STRING));
        });
  }

  private Function<QualifiedName, Mono<Activity>> createNewTokenGeneration(Token tokenModel) {
    return (QualifiedName identifier) -> Mono.justOrEmpty(identifier)
        .map(provFactory::newActivity)
        .doOnNext(activity -> {
          XMLGregorianCalendar timestampVal = ProvUtilities
              .toXMLGregorianCalendar(Date.from(Instant.ofEpochSecond(tokenModel.getCreatedOn())));
          activity.setStartTime(timestampVal);
          activity.setEndTime(timestampVal);

          activity.getType().add(provFactory.newType(
              cpmProvFactory.newCpmQualifiedName("tokenGeneration"),
              provFactory.getName().PROV_TYPE));
        });
  }

  private Function<QualifiedName, Mono<Entity>> createNewToken(Token tokenModel) {
    return (QualifiedName identifier) -> Mono.justOrEmpty(identifier)
        .map(provFactory::newEntity)
        .doOnNext(tokenEntity -> {
          tokenEntity.getType().add(provFactory.newType(
              cpmProvFactory.newCpmQualifiedName("token"),
              provFactory.getName().PROV_TYPE));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("originatorId"),
              tokenModel.getAdditionalData().getOrganizationIdentifier(),
              provFactory.getName().XSD_STRING));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("authorityId"),
              tokenModel.getTrustedParty().getName(),
              provFactory.getName().XSD_STRING));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("tokenTimestamp"),
              tokenModel.getCreatedOn(),
              provFactory.getName().XSD_LONG));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("documentCreationTimestamp"),
              tokenModel.getAdditionalData().getDocumentTimestamp(),
              provFactory.getName().XSD_LONG));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("documentDigest"),
              tokenModel.getHash(),
              provFactory.getName().XSD_STRING));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("bundle"),
              tokenModel.getAdditionalData().getBundle(),
              provFactory.getName().XSD_STRING));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("hashFunction"),
              tokenModel.getAdditionalData().getHashFunction(),
              provFactory.getName().XSD_STRING));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("trustedPartyUri"),
              tokenModel.getAdditionalData().getTrustedPartyUri(),
              provFactory.getName().XSD_STRING));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("trustedPartyCertificate"),
              tokenModel.getAdditionalData().getTrustedPartyCertificate(),
              provFactory.getName().XSD_STRING));

          tokenEntity.getOther().add(provFactory.newOther(
              cpmProvFactory.newCpmQualifiedName("signature"),
              tokenModel.getSignature(),
              provFactory.getName().XSD_STRING));
        });

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

  private Function<QualifiedName, QualifiedName> generateIdentifierFrom(String local) {
    return (QualifiedName identifier) -> provFactory.newQualifiedName(
        identifier.getNamespaceURI(),
        local,
        identifier.getPrefix());
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
        .single();
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
          .filter(
              other -> other.getElementName().getLocalPart().equals("version")
                  && other.getElementName().getNamespaceURI().equals("http://purl.org/pav/")
                  && other.getElementName().getPrefix().equals("pav"))

          .map(Other::getValue)
          .filter(LangString.class::isInstance)
          .map(LangString.class::cast)
          .map(LangString::getValue)
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
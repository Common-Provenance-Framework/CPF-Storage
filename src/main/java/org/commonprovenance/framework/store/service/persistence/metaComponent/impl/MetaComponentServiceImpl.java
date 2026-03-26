package org.commonprovenance.framework.store.service.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.vanilla.ProvUtilities;
import org.springframework.stereotype.Service;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.model.ICpmProvFactory;
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
  public Mono<Document> createMetaComponent(QualifiedName metaBundleIdentifier) {
    Entity generalEntity = this.provFactory.newEntity(
        this.provFactory.newQualifiedName(
            this.configuration.getFqdn() + "documents/", UUID.randomUUID().toString(), "storage"),
        List.of(provFactory.newType(
            provFactory.getName().PROV_BUNDLE,
            provFactory.getName().PROV_QUALIFIED_NAME)));

    Namespace bundleNs = provFactory.newNamespace();
    bundleNs.register("meta", this.configuration.getFqdn() + "documents/meta/");

    Bundle bundle = provFactory.newNamedBundle(metaBundleIdentifier, bundleNs, List.of(generalEntity));

    Document provDocument = this.provFactory.newDocument();
    provDocument.getNamespace().addKnownNamespaces();
    provDocument.getNamespace().register(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
    provDocument.getNamespace().register("pav", "http://purl.org/pav/");
    provDocument.getNamespace().register("meta", this.configuration.getFqdn() + "documents/meta/");
    provDocument.getNamespace().register("storage", this.configuration.getFqdn() + "documents/");
    provDocument.getStatementOrBundle().add(bundle);

    return this.bundlePersistence.create(provDocument);

  }

  private Function<Entity, Mono<Entity>> addNextVersion(Document document, QualifiedName nextVersionIdentifier) {
    return (Entity lastVersion) -> this.getBundleIdentifier(document)
        .map(QualifiedName::getLocalPart)
        .flatMap((String identifier) -> Mono.zip(
            this.entityPersistence.getGeneralVersionEntity(identifier),
            this.entityPersistence.getLastVersion(identifier)
                .onErrorResume(NotFoundException.class, _ -> Mono.just(0))
                .map(this::incrementVersion)
                .map(this.createNewVersion(document.getNamespace().getNamespaces(), nextVersionIdentifier))))
        .flatMap(tuple -> {
          Entity general = tuple.getT1();
          Entity newVersion = tuple.getT2();
          return entityPersistence.addNewVersion(general, lastVersion).apply(newVersion);
        });
  }

  private Mono<Entity> addFirstVersion(Document document, QualifiedName nextVersionIdentifier) {
    return this.getBundleIdentifier(document)
        .map(QualifiedName::getLocalPart)
        .flatMap((String identifier) -> Mono.zip(
            this.entityPersistence.getGeneralVersionEntity(identifier),
            this.entityPersistence.getLastVersion(identifier)
                .onErrorResume(NotFoundException.class, _ -> Mono.just(0))
                .map(this::incrementVersion)
                .map(this.createNewVersion(document.getNamespace().getNamespaces(), nextVersionIdentifier))))
        .flatMap(tuple -> {
          Entity general = tuple.getT1();
          Entity newVersion = tuple.getT2();
          return entityPersistence.addFirstVersion(general).apply(newVersion);
        });
  }

  @Override
  public Function<Document, Mono<Document>> addNewVersion(QualifiedName identifier) {
    return (Document document) -> {
      return this.getBundleIdentifier(document)
          .map(QualifiedName::getLocalPart)
          .flatMap(this.entityPersistence::getLastVersionEntity)
          .flatMap(this.addNextVersion(document, identifier))
          .onErrorResume(NotFoundException.class, _ -> this.addFirstVersion(document, identifier))
          .then(this.getBundleIdentifier(document)
              .map(QualifiedName::getLocalPart)
              .flatMap(this.bundlePersistence::getByIdentifier));
    };

  }

  @Override
  public Function<Document, Mono<Document>> addTokenToLastVersion(Token tokenModel) {
    return (Document document) -> {
      Mono<Entity> lastVersionEntity = this.getBundleIdentifier(document)
          .map(QualifiedName::getLocalPart)
          .flatMap(this.entityPersistence::getLastVersionEntity)
          .cache();// TODO: Mono is lazy and cold!!! Need to be refactored!!!

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
          .then(this.getBundleIdentifier(document)
              .map(QualifiedName::getLocalPart)
              .flatMap(this.bundlePersistence::getByIdentifier));
    };
  }

  @Override
  public Mono<Document> getMetaComponent(QualifiedName metaBundleId) {
    return Mono.justOrEmpty(metaBundleId)
        .flatMap(this::getByIdentifier)
        .onErrorResume(NotFoundException.class,
            _ -> this.createMetaComponent(metaBundleId));
  }

  @Override
  public Mono<Document> getByIdentifier(QualifiedName id) {
    return this.bundlePersistence.getByIdentifier(id.getLocalPart());
  }

  @Override
  public Mono<Boolean> exists(String id) {
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

  private Integer incrementVersion(Integer version) {
    return version + 1;
  }

  private Function<QualifiedName, QualifiedName> generateIdentifierFrom(String local) {
    return (QualifiedName identifier) -> provFactory.newQualifiedName(
        identifier.getNamespaceURI(),
        local,
        identifier.getPrefix());
  }

  private Mono<QualifiedName> getBundleIdentifier(Document document) {
    return this.getBundle(document)
        .map(Bundle::getId);
  }

  private Mono<Bundle> getBundle(Document document) {
    return Flux.fromIterable(document.getStatementOrBundle())
        .filter(Bundle.class::isInstance)
        .map(Bundle.class::cast)
        .single()
        .onErrorResume(MONO.exceptionWrapper("MetaComponentService - Error while getting Bundle"));

  }

}
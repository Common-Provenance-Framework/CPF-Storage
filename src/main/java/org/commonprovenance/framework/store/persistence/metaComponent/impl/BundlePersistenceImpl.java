package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.common.utils.JwtUtils;
import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.ProvenanceFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.vanilla.ProvUtilities;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

@Component
public class BundlePersistenceImpl implements BundlePersistence {

  private final BundleRepository bundleRepository;
  private final EntityRepository entityRepository;
  private final AppConfiguration configuration;

  public BundlePersistenceImpl(
      BundleRepository bundleRepository,
      EntityRepository entityRepository,
      AppConfiguration configuration) {
    this.bundleRepository = bundleRepository;
    this.entityRepository = entityRepository;
    this.configuration = configuration;
  }

  @Override
  public Mono<Document> create(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(identifier)
        .flatMap(MONO.makeSureAsync(
            this::notExists,
            id -> new ConflictException("Bundle with identifier '" + id + "' exists in database!")))
        .map(BundleNode::new)
        // TODO: Create factory method for this
        .map(bundle -> bundle.withEntity(new EntityNode(UUID.randomUUID().toString(), "prov:Bundle")))
        .flatMap(bundleRepository::save)
        .onErrorResume(MONO.exceptionWrapper("BundlePersistence - Error while creating new Bundle"))
        .flatMap(ProvenanceFactory.bundleToProv(this.configuration));
  }

  @Override
  public Function<String, Mono<Document>> createFirstVersion(String identifier) {
    return (String versionIdenifier) -> MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!")
        .apply(identifier)
        .flatMap(MONO.makeSureAsync(
            bundleRepository::hasNotVersionEntity,
            id -> new ConflictException("Bundle with identifier '" + id + "' has at least one version Entity")))
        .flatMap(bundleIdentifier -> Mono.zip(
            bundleRepository.findByIdentifier(bundleIdentifier),
            entityRepository.getGeneralEntityByBundleIdentifier(bundleIdentifier)))
        .map(this.buildBundleWithFirstVersion(versionIdenifier))
        .flatMap(this.bundleRepository::save)
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public Function<String, Mono<Document>> createVersion(String identifier) {
    return (String versionIdenifier) -> MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!")
        .apply(identifier)
        .flatMap(MONO.makeSureAsync(
            bundleRepository::hasVersionEntity,
            id -> new ConflictException("Bundle with identifier '" + id + "' has no version Entity")))
        .flatMap(bundleIdentifier -> Mono.zip(
            bundleRepository.findByIdentifier(bundleIdentifier),
            entityRepository.getGeneralEntityByBundleIdentifier(bundleIdentifier),
            entityRepository.getLastVersionEntityByBundleIdentifier(identifier)))
        .map(this.buildBundleWithVersion(versionIdenifier))
        .flatMap(this.bundleRepository::save)
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public Function<String, Mono<Document>> createNewVersion(String identifier) {
    return (String versionIdenifier) -> MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!")
        .apply(identifier)
        .flatMap(MONO.makeSureAsync(
            bundleRepository::hasVersionEntity,
            id -> new ConflictException("Bundle with identifier '" + id + "' has no version Entity")))
        .flatMap(this.createVersionFlipFunction(versionIdenifier))
        .onErrorResume(
            ConflictException.class,
            _ -> this.createFirstVersionSimple(identifier, versionIdenifier));
  }

  @Override
  public Function<Token, Mono<Document>> createToken(String identifier) {
    return (Token token) -> MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!")
        .apply(identifier)
        .flatMap(entityRepository::getLastVersionEntityByBundleIdentifier)
        .flatMap(MONO.<EntityNode>makeSureAsync(
            this::hasNotToken,
            _ -> new ConflictException("Last version entity already has Token!")))
        .flatMap((EntityNode lastVersion) -> Mono.zip(
            bundleRepository.findByIdentifier(identifier),
            Mono.just(lastVersion)))
        .map(this.buildBundleWithToken(token))
        .flatMap(this.bundleRepository::save)
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public Mono<Document> getByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(identifier)
        .flatMap(bundleRepository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("BundlePersistence - Error while reading bundle"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Bundle with identifier '" + identifier + "' has not been found!"))))
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public Mono<Boolean> exists(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(identifier)
        .flatMap(bundleRepository::exists)
        .onErrorResume(MONO.exceptionWrapper("BundlePersistence - Error while checking Bundle"));
  }

  @Override
  public Mono<Boolean> notExists(String identifier) {
    return this.exists(identifier).map(v -> !v);
  }

  private Mono<Document> createFirstVersionSimple(String bundleIdenifier, String versionIdenifier) {
    return Mono.just(versionIdenifier)
        .flatMap(this.createFirstVersion(bundleIdenifier));
  }

  private Function<String, Mono<Document>> createVersionFlipFunction(String versionIdenifier) {
    return (String bundleIdenifier) -> Mono.just(versionIdenifier)
        .flatMap(this.createVersion(bundleIdenifier));
  }

  private Function<Tuple2<BundleNode, EntityNode>, BundleNode> buildBundleWithFirstVersion(String versionIdenifier) {
    return (Tuple2<BundleNode, EntityNode> tuple) -> {
      BundleNode bundle = tuple.getT1();
      EntityNode generalEntity = tuple.getT2();

      // TODO: Create factory method for this
      EntityNode firstVersion = new EntityNode(versionIdenifier, "prov:Bundle")
          .withVersion(1)
          .withSpecializationOfEntity(generalEntity);

      return bundle.withEntity(firstVersion);
    };
  }

  private Function<Tuple3<BundleNode, EntityNode, EntityNode>, BundleNode> buildBundleWithVersion(
      String versionIdenifier) {
    return (Tuple3<BundleNode, EntityNode, EntityNode> tuple) -> {
      BundleNode bundle = tuple.getT1();
      EntityNode generalEntity = tuple.getT2();
      EntityNode lastVersion = tuple.getT3();
      Integer lastVersionNo = Integer.valueOf(lastVersion.getPav().get("version").toString());

      // TODO: Create factory method for this
      EntityNode newVersion = new EntityNode(versionIdenifier, "prov:Bundle")
          .withVersion(lastVersionNo + 1)
          .withSpecializationOfEntity(generalEntity)
          .withRevisionOfEntity(lastVersion);

      return bundle.withEntity(newVersion);
    };
  }

  private Mono<Boolean> hasToken(EntityNode version) {
    return Mono.just(version)
        .map(EntityNode::getIdentifier)
        .flatMap(entityRepository::getTokenByVersionEntityIdentifier)
        .hasElement();
  }

  private Mono<Boolean> hasNotToken(EntityNode version) {
    return Mono.just(version)
        .flatMap(this::hasToken)
        .map(v -> !v);
  }

  private Function<Tuple2<BundleNode, EntityNode>, BundleNode> buildBundleWithToken(Token token) {
    return (Tuple2<BundleNode, EntityNode> tuple) -> {
      BundleNode bundle = tuple.getT1();
      EntityNode versionEntity = tuple.getT2();

      XMLGregorianCalendar timestampVal = ProvUtilities
          .toXMLGregorianCalendar(Date.from(Instant.ofEpochSecond(token.getCreatedOn())));

      // TODO: Create factory method for this
      AgentNode tokenGeneratorNode = new AgentNode(
          token.getTrustedParty().getName(),
          // TODO: Create Enum for konown types
          "cpm:TrustedParty",
          JwtUtils.extractTokenGeneratorAttributes(token.getJwt()));

      // TODO: Create factory method for this
      ActivityNode tokenGenerationNode = new ActivityNode(
          UUID.randomUUID().toString(),
          // TODO: Create Enum for konown types
          "cpm:TokenGeneration",
          timestampVal.toString(),
          timestampVal.toString())
          .withUsedEntity(versionEntity)
          .withWasAssociatedWithAgent(tokenGeneratorNode);

      // TODO: Create factory method for this
      EntityNode tokenNode = new EntityNode(
          UUID.randomUUID().toString(),
          // TODO: Create Enum for konown types
          "cpm:Token",
          Map.of("jwt", token.getJwt()))
          .withWasDerivedFromEntity(versionEntity)
          .withWasGeneratedByActivity(tokenGenerationNode)
          .withWasAttributedToAgent(tokenGeneratorNode);

      return bundle
          .withEntity(tokenNode)
          .withActivity(tokenGenerationNode)
          .withAgent(tokenGeneratorNode);
    };
  }

}

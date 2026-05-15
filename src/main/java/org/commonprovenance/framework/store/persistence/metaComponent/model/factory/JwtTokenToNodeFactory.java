package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.Map;
import java.util.UUID;

import org.commonprovenance.framework.store.common.utils.JwtUtils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import io.vavr.control.Either;

public final class JwtTokenToNodeFactory {
  public static Either<ApplicationException, EntityNode> toTokenEntity(String jwtToken) {
    return JwtTokenToNodeFactory.toTokenGenerationActivity(jwtToken)
        .flatMap(tokenGenerationNode -> EITHER.<AgentNode, ActivityNode, EntityNode> combine(
            EITHER.liftEitherChecked(tokenGenerationNode::getTokenGenerator),
            Either.right(tokenGenerationNode),
            (generator, generation) -> new EntityNode(
                UUID.randomUUID().toString(),
                // TODO: Create Enum for konown types
                "cpm:Token",
                Map.of("jwt", jwtToken))
                .withWasGeneratedByActivity(generation)
                .withWasAttributedToAgent(generator)));
  }

  public static Either<ApplicationException, AgentNode> toTokenGeneratorAgent(String jwtToken) {
    return EITHER.<Map<String, Object>, String, AgentNode> combine(
        Either.<ApplicationException, String> right(jwtToken)
            .flatMap(JwtUtils::extractTokenGeneratorAttributes),
        Either.<ApplicationException, String> right(jwtToken)
            .flatMap(JwtUtils::extractTokenGeneratorIdentifier),
        (cpmAttrs, authorityId) -> new AgentNode(
            authorityId,
            // TODO: Create Enum for konown types
            "cpm:TrustedParty",
            cpmAttrs));
  }

  public static Either<ApplicationException, ActivityNode> toTokenGenerationActivity(String jwtToken) {
    return EITHER.<AgentNode, String, ActivityNode> combine(
        toTokenGeneratorAgent(jwtToken),
        JwtUtils.extractTokenCreationString(jwtToken),
        (generator, createdOn) -> new ActivityNode(
            UUID.randomUUID().toString(),
            // TODO: Create Enum for konown types
            "cpm:TokenGeneration",
            createdOn,
            createdOn)
            .withWasAssociatedWithAgent(generator));
  }

}

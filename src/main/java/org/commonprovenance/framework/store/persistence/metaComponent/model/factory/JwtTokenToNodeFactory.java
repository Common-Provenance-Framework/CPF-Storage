package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.Map;
import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import io.vavr.control.Either;

public final class JwtTokenToNodeFactory {
  public static Either<ApplicationException, EntityNode> toTokenEntity(Token token) {
    return JwtTokenToNodeFactory.toTokenGenerationActivity(token)
        .flatMap(tokenGenerationNode -> EITHER.<AgentNode, ActivityNode, EntityNode> combine(
            EITHER.liftEitherChecked(tokenGenerationNode::getTokenGenerator),
            Either.right(tokenGenerationNode),
            (generator, generation) -> new EntityNode(
                UUID.randomUUID().toString(),
                // TODO: Create Enum for konown types
                "cpm:Token",
                Map.of("jwt", token.getJwt()))
                .withWasGeneratedByActivity(generation)
                .withWasAttributedToAgent(generator)));
  }

  public static Either<ApplicationException, AgentNode> toTokenGeneratorAgent(Token token) {
    return EITHER.<Map<String, Object>, String, AgentNode> combine(
        Either.<ApplicationException, Token> right(token)
            .flatMap(Token::getTokenGeneratorAttributes),
        Either.<ApplicationException, Token> right(token)
            .flatMap(Token::getTokenGeneratorIdentifier),
        (cpmAttrs, authorityId) -> new AgentNode(
            authorityId,
            // TODO: Create Enum for konown types
            "cpm:TrustedParty",
            cpmAttrs));
  }

  public static Either<ApplicationException, ActivityNode> toTokenGenerationActivity(Token token) {
    return EITHER.<AgentNode, String, ActivityNode> combine(
        toTokenGeneratorAgent(token),
        token.getTokenTimestampAsString(),
        (generator, createdOn) -> new ActivityNode(
            UUID.randomUUID().toString(),
            // TODO: Create Enum for konown types
            "cpm:TokenGeneration",
            createdOn,
            createdOn)
            .withWasAssociatedWithAgent(generator));
  }

}

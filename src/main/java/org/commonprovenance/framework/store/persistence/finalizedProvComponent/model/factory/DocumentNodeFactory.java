package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;

public class DocumentNodeFactory {

  // TODO:
  public static DocumentNode build(Document document) {
    return new DocumentNode(
        document.getIdentifier().get(),
        document.getGraph(),
        document.getFormat().toString());
  }

  public static DocumentNode buildWithRelations(Document document) {
    return build(document)
        .withToken(document.getToken().map(TokenNodeFactory::build));
  }

  public static DocumentNode buildWithFullRelations(Document document) {
    return build(document)
        .withToken(document.getToken().map(TokenNodeFactory::buildWithRelations));
  }

}

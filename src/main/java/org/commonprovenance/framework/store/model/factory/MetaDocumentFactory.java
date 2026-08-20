package org.commonprovenance.framework.store.model.factory;

import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.openprovenance.prov.model.Document;

public class MetaDocumentFactory {
  public static MetaDocument build(Document document) {
    return new MetaDocument(document, GraphFormat.JSON);
  }
}

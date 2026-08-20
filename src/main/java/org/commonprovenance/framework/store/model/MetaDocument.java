package org.commonprovenance.framework.store.model;

import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasMetaDocument;
import org.commonprovenance.framework.store.common.dto.HasTokenOptional;
import org.commonprovenance.framework.store.common.validation.DTOValidator;
import org.openprovenance.prov.model.Document;

public class MetaDocument extends DTOValidator implements
    HasMetaDocument<MetaDocument>,
    HasTokenOptional<MetaDocument> {
  private final Document document;
  private final GraphFormat format;

  private final Optional<Token> token;

  public MetaDocument(
      Document document,
      GraphFormat format) {
    this.document = document;
    this.format = format;
    this.token = Optional.empty();
  }

  public MetaDocument(
      Document document,
      GraphFormat format,
      Token token) {
    this.document = document;
    this.format = format;

    this.token = Optional.ofNullable(token);
  }

  public MetaDocument() {
    this.document = null;
    this.format = null;

    this.token = Optional.empty();
  }

  public MetaDocument withDocument(Document document) {
    return new MetaDocument(
        document,
        this.getFormat(),
        this.getToken().orElse(null));
  }

  public MetaDocument withFormat(GraphFormat format) {
    return new MetaDocument(
        this.getDocument(),
        format,
        this.getToken().orElse(null));
  }

  public MetaDocument withToken(Token token) {
    return new MetaDocument(
        this.getDocument(),
        this.getFormat(),
        token);
  }

  public Document getDocument() {
    return document;
  }

  public GraphFormat getFormat() {
    return format;
  }

  public Optional<Token> getToken() {
    return token;
  }

}

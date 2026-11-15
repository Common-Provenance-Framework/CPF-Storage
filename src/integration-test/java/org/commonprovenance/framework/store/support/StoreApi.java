package org.commonprovenance.framework.store.support;

public final class StoreApi {
  public static final String ORGANIZATIONS = "/api/v1/organizations";
  public static final String ORGANIZATION = ORGANIZATIONS + "/{organizationIdentifier}";
  public static final String DOCUMENTS = ORGANIZATION + "/documents";
  public static final String DOCUMENT = DOCUMENTS + "/{identifier}";
  public static final String DOCUMENT_TOKEN = DOCUMENT + "/token";
  public static final String DOCUMENT_DOMAIN_SPECIFIC = DOCUMENT + "/domain-specific";
  public static final String DOCUMENT_BACKBONE = DOCUMENT + "/backbone";
  public static final String META_DOCUMENT = "/api/v1/documents/meta/{identifier}";

  private StoreApi() {
  }
}

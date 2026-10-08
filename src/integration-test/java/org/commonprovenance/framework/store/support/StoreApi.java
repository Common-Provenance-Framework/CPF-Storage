package org.commonprovenance.framework.store.support;

public final class StoreApi {
  public static final String ORGANIZATIONS = "/api/v1/organizations";
  public static final String ORGANIZATION = ORGANIZATIONS + "/{organizationIdentifier}";
  public static final String DOCUMENTS = ORGANIZATION + "/documents";
  public static final String DOCUMENT = DOCUMENTS + "/{identifier}";
  public static final String DOCUMENT_TOKEN = DOCUMENT + "/token";

  private StoreApi() {
  }
}

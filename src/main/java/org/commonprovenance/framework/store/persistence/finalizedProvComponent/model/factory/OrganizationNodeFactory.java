package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;

public class OrganizationNodeFactory {

  public static OrganizationNode build(Organization organization) {
    return new OrganizationNode(organization.getIdentifier(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  public static OrganizationNode buildWithRelations(Organization organization) {
    return build(organization)
        .withTrustedParty(organization.getTrustedParty().map(TrustedPartyNodeFactory::build))
        .withDocument(organization.getDocument().map(DocumentNodeFactory::build));
  }

  public static OrganizationNode buildWithFullRelations(Organization organization) {
    return build(organization)
        .withTrustedParty(organization.getTrustedParty().map(TrustedPartyNodeFactory::build))
        .withDocument(organization.getDocument().map(DocumentNodeFactory::buildWithRelations));
  }

}

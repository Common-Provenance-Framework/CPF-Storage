package org.commonprovenance.framework.store.web.trustedParty;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

/**
 * Provides reactive operations for registering, updating, and retrieving organizations through a
 * trusted-party service.
 */
public interface OrganizationWeb {

  /**
   * Registers an {@link Organization} with its trusted-party service.
   *
   * @param organization organization to register
   * @return a publisher that completes when registration succeeds, or emits an error on failure
   */
  Mono<Void> create(Organization organization);

  /**
   * Updates the certificates associated with an {@link Organization} at its trusted-party service.
   *
   * @param organization organization whose certificates should be updated
   * @return a publisher that completes when the update succeeds, or emits an error on failure
   */
  Mono<Void> updateOrganizationCertificate(Organization organization);

  /**
   * Retrieves an organization by its identifier from its trusted-party service.
   *
   * @param organization organization providing the identifier and trusted-party details
   * @return a publisher that emits the retrieved {@link Organization}, or an error if it cannot be
   *     retrieved
   */
  Mono<Organization> getById(Organization organization);
}

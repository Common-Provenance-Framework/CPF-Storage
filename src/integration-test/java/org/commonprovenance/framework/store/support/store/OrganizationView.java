package org.commonprovenance.framework.store.support.store;

import java.util.List;

public record OrganizationView(String id, String clientCertificate, List<String> intermediateCertificates) {
}

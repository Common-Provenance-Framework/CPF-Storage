package org.commonprovenance.framework.store.common.validation;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.vanilla.ProvFactory;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.merged.CpmMergedFactory;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import io.vavr.control.Either;

@DisplayName("Validator - CPMAttributesValidator")
@ExtendWith(MockitoExtension.class)
class CPMAttributesValidatorTest {

  private static final String STORE_FQDN = "https://store.example.org/api/v1/";
  private static final String ORGANIZATION_ID = "organization";
  private static final String DOCUMENTS = STORE_FQDN + "organizations/" + ORGANIZATION_ID + "/documents/";
  private static final String EXAMPLE = "https://www.example.com/";

  private final ProvFactory provFactory = new ProvFactory();

  private final Other referencedBundleId = attribute(CpmAttribute.REFERENCED_BUNDLE_ID, exampleName("bundle"));
  private final Other referencedMetaBundleId = attribute(CpmAttribute.REFERENCED_META_BUNDLE_ID, exampleName("bundle_meta"));
  private final Other referencedBundleSpecV = attribute(CpmAttribute.REFERENCED_BUNDLE_SPECV, "1.0");
  private final Other referencedMetaBundleSpecV = attribute(CpmAttribute.REFERENCED_META_BUNDLE_SPECV, "1.0");
  private final Other referencedBundleHashValue = attribute(CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE, "4e0740856");
  private final Other hashAlg = attribute(CpmAttribute.HASH_ALG, "SHA256");

  @Mock
  private AppConfiguration configuration;

  @BeforeEach
  void setUp() {
    when(configuration.getFqdn()).thenReturn(STORE_FQDN);
  }

  private QualifiedName exampleName(String localPart) {
    return provFactory.newQualifiedName(EXAMPLE, localPart, "ex");
  }

  private QualifiedName cpmName(String localPart) {
    return provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, localPart, CpmNamespaceConstants.CPM_PREFIX);
  }

  private Type cpmType(CpmType type) {
    return provFactory.newType(cpmName(type.toString()), provFactory.getName().PROV_QUALIFIED_NAME);
  }

  private Other attribute(CpmAttribute attribute, QualifiedName value) {
    return provFactory.newOther(cpmName(attribute.toString()), value, provFactory.getName().PROV_QUALIFIED_NAME);
  }

  private Other attribute(CpmAttribute attribute, String value) {
    return provFactory.newOther(cpmName(attribute.toString()), value, provFactory.getName().XSD_STRING);
  }

  private Activity mainActivity(Other... attributes) {
    Activity activity = provFactory.newActivity(exampleName("mainActivity"));
    activity.getType().add(cpmType(CpmType.MAIN_ACTIVITY));
    activity.getOther().addAll(List.of(attributes));
    return activity;
  }

  private Entity connector(String localPart, CpmType type, Other... attributes) {
    Entity connector = provFactory.newEntity(exampleName(localPart));
    connector.getType().add(cpmType(type));
    connector.getOther().addAll(List.of(attributes));
    return connector;
  }

  private Entity backwardConnector(Other... attributes) {
    return connector("backwardConnector", CpmType.BACKWARD_CONNECTOR, attributes);
  }

  private Entity specForwardConnector(Other... attributes) {
    return connector("specForwardConnector", CpmType.SPEC_FORWARD_CONNECTOR, attributes);
  }

  private Organization organization(Statement... statements) {
    CpmDocument component = new CpmDocument(
        provFactory.newDocument(
            provFactory.newNamespace(),
            List.of(provFactory.newNamedBundle(
                provFactory.newQualifiedName(DOCUMENTS, "component", "storage"),
                provFactory.newNamespace(),
                List.of(statements)))),
        provFactory,
        new CpmProvFactory(provFactory),
        new CpmMergedFactory());
    return new Organization(ORGANIZATION_ID, null, List.of(), null, new Document(null, GraphFormat.JSON, component, null));
  }

  private Either<ApplicationException, Void> validate(Organization organization) {
    return CPMAttributesValidator.validate(configuration).apply(organization);
  }

  private void assertRejected(Either<ApplicationException, Void> result) {
    assertTrue(result.isLeft(), "the component should be rejected");
    assertInstanceOf(InvalidValueException.class, result.getLeft());
  }

  @Test
  @DisplayName("should accept a component whose main activity and referencing connectors carry their spec versions")
  void validate_shouldAcceptComponentCarryingEverySpecVersion() {
    Organization organization = organization(
        mainActivity(referencedMetaBundleId, referencedMetaBundleSpecV),
        backwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg),
        specForwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg));

    assertTrue(validate(organization).isRight(), "the component should be accepted");
  }

  @Test
  @DisplayName("should reject a backward connector without referencedBundleSpecV")
  void validate_shouldRejectBackwardConnectorWithoutReferencedBundleSpecV() {
    Organization organization = organization(
        mainActivity(referencedMetaBundleId, referencedMetaBundleSpecV),
        backwardConnector(referencedBundleId, referencedMetaBundleId, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg),
        specForwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg));

    assertRejected(validate(organization));
  }

  @Test
  @DisplayName("should reject a backward connector without referencedMetaBundleSpecV")
  void validate_shouldRejectBackwardConnectorWithoutReferencedMetaBundleSpecV() {
    Organization organization = organization(
        mainActivity(referencedMetaBundleId, referencedMetaBundleSpecV),
        backwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV,
            referencedBundleHashValue, hashAlg),
        specForwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg));

    assertRejected(validate(organization));
  }

  @Test
  @DisplayName("should reject a specialized forward connector without referencedBundleSpecV")
  void validate_shouldRejectSpecForwardConnectorWithoutReferencedBundleSpecV() {
    Organization organization = organization(
        mainActivity(referencedMetaBundleId, referencedMetaBundleSpecV),
        backwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg),
        specForwardConnector(referencedBundleId, referencedMetaBundleId, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg));

    assertRejected(validate(organization));
  }

  @Test
  @DisplayName("should reject a specialized forward connector without referencedMetaBundleSpecV")
  void validate_shouldRejectSpecForwardConnectorWithoutReferencedMetaBundleSpecV() {
    Organization organization = organization(
        mainActivity(referencedMetaBundleId, referencedMetaBundleSpecV),
        backwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg),
        specForwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV,
            referencedBundleHashValue, hashAlg));

    assertRejected(validate(organization));
  }

  @Test
  @DisplayName("should reject a main activity without referencedMetaBundleSpecV")
  void validate_shouldRejectMainActivityWithoutReferencedMetaBundleSpecV() {
    Organization organization = organization(
        mainActivity(referencedMetaBundleId),
        backwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg),
        specForwardConnector(referencedBundleId, referencedMetaBundleId, referencedBundleSpecV, referencedMetaBundleSpecV,
            referencedBundleHashValue, hashAlg));

    assertRejected(validate(organization));
  }
}

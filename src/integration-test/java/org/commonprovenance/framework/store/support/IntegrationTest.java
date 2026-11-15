package org.commonprovenance.framework.store.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.commonprovenance.framework.store.support.otherStore.OtherStoreStub;
import org.commonprovenance.framework.store.support.store.StoreClient;
import org.commonprovenance.framework.store.support.trustedParty.TrustedPartyStub;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@ContextConfiguration(initializers = { Neo4jContainerInitializer.class, WireMockInitializer.class })
@Import({ IntegrationTestData.class, StoreClient.class, TrustedPartyStub.class, OtherStoreStub.class })
@ExtendWith(WireMockResetExtension.class)
public @interface IntegrationTest {
}

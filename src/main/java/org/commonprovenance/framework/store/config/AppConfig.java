package org.commonprovenance.framework.store.config;

import org.commonprovenance.framework.store.common.composition.Reactor.ReactorComposition;
import org.commonprovenance.framework.store.common.composition.EitherUtils.EitherComposition;
import org.openprovenance.prov.vanilla.ProvFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import cz.muni.fi.cpm.merged.CpmMergedFactory;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;

@Configuration
public class AppConfig {
  @Bean
  public ProvFactory provFactory() {
    return new ProvFactory();
  }

  @Bean
  public ICpmFactory cpmFactory() {
    return new CpmMergedFactory();
  }

  @Bean
  public ICpmProvFactory cpmProvFactory(ProvFactory provFactory) {
    return new CpmProvFactory(provFactory);
  }

  @Bean
  public AppConfiguration loadConfiguration(Environment env) {
    AppConfiguration appConfiguration = new AppConfiguration(
        env.getProperty(
            "store.url",
            String.class,
            "http://localhost:8080/api/v1/"),
        env.getProperty(
            "store.mode.verbose",
            Boolean.class,
            false));

    EitherComposition.initialize(appConfiguration.isVerboseMode());
    ReactorComposition.initialize(appConfiguration.isVerboseMode());

    return appConfiguration;
  }
}

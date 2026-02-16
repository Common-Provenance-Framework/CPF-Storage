package org.commonprovenance.framework.store.config;

import org.openprovenance.prov.vanilla.ProvFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}

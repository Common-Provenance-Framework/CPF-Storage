package org.commonprovenance.framework.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class CpfStorageApplication {

  public static void main(String[] args) {
    SpringApplication.run(CpfStorageApplication.class, args);
  }

}

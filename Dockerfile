FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
COPY src/main/resources/application.properties.tmp src/main/resources/application.properties
COPY src/main/resources/application-neo4j.properties.tmp  src/main/resources/application-neo4j.properties

RUN chmod +x mvnw
RUN ./mvnw -B org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file \
    -Dfile=src/main/resources/CPF-Toolbox/cpm-core-2.2.0.jar \
    -DgroupId=cz.muni.fi.cpm \
    -DartifactId=cpm-core \
    -Dversion=2.2.0 \
    -Dpackaging=jar \
    -DgeneratePom=true
RUN ./mvnw -B org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file \
    -Dfile=src/main/resources/CPF-Toolbox/cpm-template-2.2.0.jar \
    -DgroupId=cz.muni.fi.cpm \
    -DartifactId=cpm-template \
    -Dversion=2.2.0 \
    -Dpackaging=jar \
    -DgeneratePom=true
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:25-jdk AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
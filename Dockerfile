FROM eclipse-temurin:25-jdk@sha256:8c0a84ea11c8f6ed52600fc19f1040121f2a162998e9f50a5faebbbad9172dcc AS build

WORKDIR /workspace

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -B org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file \
    -Dfile=src/main/resources/CPF-Toolbox/cpm-core-2.5.0.jar \
    -DgroupId=cz.muni.fi.cpm \
    -DartifactId=cpm-core \
    -Dversion=2.5.0 \
    -Dpackaging=jar \
    -DgeneratePom=true
RUN ./mvnw -B org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file \
    -Dfile=src/main/resources/CPF-Toolbox/cpm-template-2.5.0.jar \
    -DgroupId=cz.muni.fi.cpm \
    -DartifactId=cpm-template \
    -Dversion=2.5.0 \
    -Dpackaging=jar \
    -DgeneratePom=true
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:27-jre-alpine-3.24@sha256:9d76de111573e77ac415d8344271a2e57dd19bd59c48725fa3bac38c101bd0ba AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
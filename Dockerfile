FROM eclipse-temurin:25-jdk@sha256:97014c4b396021f9ddb7d592a7dbedb0c4e4215c29e03dc01c393558aefb71c2 AS build

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

FROM eclipse-temurin:26-jre-alpine-3.24@sha256:2db9a5fb7c52fb44f9ffcf2d5caa16a39d4e8eb2977045ef3bb6589ae1c63ef5 AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
FROM eclipse-temurin:25-jdk@sha256:119a3d18f160a3e7655a66034d0f43beee31cd7b3b9142d57a5de29772011de6 AS build

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

FROM eclipse-temurin:26-jre-alpine-3.24@sha256:9eedff2367194d11eddd6f14101b444945a708c986270cd5716b934596ba3a31 AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
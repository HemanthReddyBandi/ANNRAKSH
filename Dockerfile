FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
RUN groupadd --system annraksh && useradd --system --gid annraksh annraksh
COPY --from=build --chown=annraksh:annraksh /workspace/target/annraksh-0.0.1-SNAPSHOT.jar app.jar
USER annraksh

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

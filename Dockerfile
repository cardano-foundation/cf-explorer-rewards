FROM maven AS build
WORKDIR /app
COPY .m2/settings.xml /root/.m2/settings.xml
COPY pom.xml /app/pom.xml
RUN mvn verify clean --fail-never
COPY . /app
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim AS runtime
COPY --from=build /app/target/*.jar /app/cardano-rewards.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "cardano-rewards.jar"]

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system portico \
    && useradd --system --gid portico --uid 10001 portico
COPY --from=build /src/target/PropertyManagementSystem-1.0-SNAPSHOT.jar /app/app.jar
USER portico
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

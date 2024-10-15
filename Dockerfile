FROM maven:3.8.5-openjdk-17 as Builder
ARG VERSION=0.0.1-SNAPSHOT
WORKDIR /build/
COPY pom.xml /build/
COPY src /build/src/

RUN mvn clean package

COPY target/atypon-food-api-${VERSION}.jar target/application.jar

FROM openjdk:17-alpine
WORKDIR /app/

COPY --from=Builder /build/target/application.jar /app/

CMD java -jar /app/application.jar
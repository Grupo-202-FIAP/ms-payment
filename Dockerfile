# ----- BUILD PHASE -----
FROM maven:3.9.9-amazoncorretto-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ----- RUNTIME PHASE -----
FROM amazoncorretto:17-alpine

WORKDIR /app

COPY --from=build /app/target/ms-payment-fastfood-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
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

ADD https://dtdg.co/latest-java-tracer /dd-java-agent.jar

COPY --from=build /app/target/app.jar app.jar

EXPOSE 8091

ENV JAVA_TOOL_OPTIONS="-javaagent:/dd-java-agent.jar"

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]

FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /target/ai-bot-0.0.1-SNAPSHOT.jar ai-bot.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","ai-bot.jar"]
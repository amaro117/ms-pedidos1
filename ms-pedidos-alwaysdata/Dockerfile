FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render inyecta PORT y no expone 8082 directamente, pero lo dejamos documentado
# para uso local con `docker run -p 8082:8082`.
EXPOSE 8082

# Por defecto corre con el perfil prod (puerto dinámico vía $PORT).
# Localmente puedes sobreescribirlo: docker run -e SPRING_PROFILES_ACTIVE=docker ...
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

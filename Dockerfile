# syntax=docker/dockerfile:1

# ---------- Etapa 1: compila o projeto ----------
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Primeiro só o que define as dependências: se o pom.xml não mudar, esta camada é reaproveitada
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Remove CRLF caso o mvnw tenha vindo do Windows e garante permissão de execução
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline

# Depois o código-fonte: mudar uma classe não baixa as dependências de novo
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests \
    && cp target/*.jar app.jar

# ---------- Etapa 2: imagem final, só com o JRE e o .jar ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Usuário sem privilégios (as capas ficam no Supabase Storage, nada é gravado no contêiner)
RUN addgroup -S sgab && adduser -S sgab -G sgab
USER sgab

COPY --from=build /app/app.jar app.jar

EXPOSE 8080
# MaxRAMPercentage: a JVM respeita o limite de memória do contêiner
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]

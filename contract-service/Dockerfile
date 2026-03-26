# ── Build stage ──────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache de dependências
COPY pom.xml ./
RUN mvn dependency:go-offline --no-transfer-progress

# Copia código e compila
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress


# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia o jar gerado
COPY --from=builder /app/target/*.jar app.jar

# Garante permissão de escrita para o H2 e logs
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
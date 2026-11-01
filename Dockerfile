# ===================================================================
# ESTAGIO 1: BUILD DA APLICACAO COM MAVEN E JDK 21
# ===================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia o pom.xml e baixa as dependencias para cachear a camada
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o codigo-fonte (incluindo as migracoes Flyway) e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================================================================
# ESTAGIO 2: RUNTIME OTIMIZADO COM JRE 21 ALPINE
# ===================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuario nao-root por seguranca (boas praticas de containerizacao)
RUN addgroup -S esggroup && adduser -S esguser -G esggroup
USER esguser:esggroup

# Copia o artefato compilado do estagio anterior
COPY --from=build /app/target/*.jar app.jar

# Porta padrao da API
EXPOSE 8080

# Variaveis de ambiente padrao (podem ser sobrescritas no docker run ou docker-compose)
ENV SERVER_PORT=8080
ENV ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
ENV ORACLE_USER=rm567059
ENV ORACLE_PASSWORD=fiap26
ENV JWT_SECRET=esg-eficiencia-energetica-secret-key-2026

ENTRYPOINT ["java", "-jar", "app.jar"]

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build


# Copy auth-service POMs
COPY pom.xml .
COPY auth-client/pom.xml ./auth-client/
COPY auth-server/pom.xml ./auth-server/

# Install mailservice-client into local Maven repo first
COPY mailservice-client/pom.xml ./mailservice-client/

COPY . .

# Build
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 spring

COPY --from=builder /build/auth-server/target/auth-server*.jar app.jar

USER spring

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8081} -jar app.jar"]
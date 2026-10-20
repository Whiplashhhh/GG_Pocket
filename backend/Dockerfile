# syntax=docker/dockerfile:1
# GG Pocket API. Build from the repository root: docker build -f backend/Dockerfile .
# Tests are not run here: the CI runs them before any image is built.

FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /src/backend
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
# Dependencies in their own layer (no cache mount: the CI layer cache keeps it while the pom stays).
RUN ./mvnw --batch-mode --quiet dependency:go-offline
COPY VERSION /src/VERSION
COPY backend/src/main/ src/main/
RUN ./mvnw --batch-mode --quiet -DskipTests -Dspotless.check.skip=true package \
    && cp target/*.jar /tmp/app.jar \
    && java -Djarmode=tools -jar /tmp/app.jar extract --layers --launcher --destination /tmp/app

FROM eclipse-temurin:25-jre-noble
ARG GIT_COMMIT=unknown
ARG GIT_BRANCH=unknown
ARG VERSION=unknown
LABEL org.opencontainers.image.title="gg-pocket-api" \
      org.opencontainers.image.description="GG Pocket API (Gravelines Grimp)" \
      org.opencontainers.image.source="https://github.com/Whiplashhhh/GG_Pocket" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"
RUN groupadd --system --gid 10001 gg \
    && useradd --system --uid 10001 --gid gg --home-dir /app --shell /usr/sbin/nologin gg \
    && mkdir -p /data/media \
    && chown -R gg:gg /data
WORKDIR /app
# Least-changing layers first: a new release usually only ships the last one.
COPY --from=build /tmp/app/dependencies/ ./
COPY --from=build /tmp/app/spring-boot-loader/ ./
COPY --from=build /tmp/app/snapshot-dependencies/ ./
COPY --from=build /tmp/app/application/ ./
ENV GG_GIT_COMMIT=${GIT_COMMIT} \
    GG_GIT_BRANCH=${GIT_BRANCH} \
    GG_MEDIA_ROOT=/data/media \
    TZ=UTC \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -XX:+UseSerialGC -Xss512k"
USER 10001:10001
VOLUME ["/data/media"]
EXPOSE 8080
# No curl in the image: bash talks HTTP to the readiness probe itself.
HEALTHCHECK --interval=10s --timeout=5s --start-period=90s --retries=5 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /actuator/health/readiness HTTP/1.1\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n' >&3 && grep -q '\"UP\"' <&3"]
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

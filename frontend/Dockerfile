# syntax=docker/dockerfile:1
# GG Pocket web: the built PWA served by Caddy, which also forwards /api to the API container.
# Build from the repository root: docker build -f frontend/Dockerfile .

FROM node:24-alpine AS build
ENV PNPM_HOME=/pnpm PATH=/pnpm:$PATH COREPACK_ENABLE_DOWNLOAD_PROMPT=0
RUN corepack enable
WORKDIR /src/frontend
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY VERSION /src/VERSION
COPY frontend/ ./
RUN pnpm build \
    && if grep -Eq '<script>|<script [^>]*>[^<]' dist/index.html; then \
         echo 'dist/index.html has an inline script: the CSP (script-src self) would block it.' >&2; exit 1; \
       fi

FROM caddy:2.10-alpine
ARG GIT_COMMIT=unknown
ARG VERSION=unknown
LABEL org.opencontainers.image.title="gg-pocket-web" \
      org.opencontainers.image.description="GG Pocket web app (Gravelines Grimp)" \
      org.opencontainers.image.source="https://github.com/Whiplashhhh/GG_Pocket" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"
RUN addgroup -S -g 10001 gg \
    && adduser -S -u 10001 -G gg -H -h /srv gg \
    && chown -R gg:gg /config /data
COPY deploy/web/Caddyfile /etc/caddy/Caddyfile
COPY --from=build /src/frontend/dist/ /srv/
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/healthz"]

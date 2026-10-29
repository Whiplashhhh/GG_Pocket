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

# Caddy rebuilt from source with the latest Go 1.27: the stock 2.11.7 binary is built with Go
# 1.26.8, whose standard library has a critical CVE (Trivy). Same Caddy version, no plugin.
# When the official image ships a fixed build, going back to its binary is enough.
FROM golang:1.27-alpine AS caddy
ENV CADDY_VERSION=v2.11.7 XCADDY_VERSION=v0.4.7 CGO_ENABLED=0
RUN apk add --no-cache git \
    && go install github.com/caddyserver/xcaddy/cmd/xcaddy@${XCADDY_VERSION} \
    && xcaddy build ${CADDY_VERSION} --output /caddy \
    && /caddy version

FROM caddy:2.11-alpine
ARG GIT_COMMIT=unknown
ARG VERSION=unknown
LABEL org.opencontainers.image.title="gg-pocket-web" \
      org.opencontainers.image.description="GG Pocket web app (Gravelines Grimp)" \
      org.opencontainers.image.source="https://github.com/Whiplashhhh/GG_Pocket" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"
# The rebuilt binary replaces the stock one. Unlike it, it carries no file capability
# (cap_net_bind_service), which the kernel would refuse to run without any capability (cap_drop: ALL
# in compose); port 8080 needs none. The Alpine packages get their security fixes too.
COPY --from=caddy /caddy /usr/bin/caddy
RUN apk upgrade --no-cache \
    && addgroup -S -g 10001 gg \
    && adduser -S -u 10001 -G gg -H -h /srv gg \
    && chown -R gg:gg /config /data
COPY deploy/web/Caddyfile /etc/caddy/Caddyfile
COPY --from=build /src/frontend/dist/ /srv/
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/healthz"]

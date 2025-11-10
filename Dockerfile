# =========================
# STAGE 1: Build the Java JAR (processor)
# =========================
FROM maven:3.9-eclipse-temurin-24 AS java-builder
WORKDIR /build/processor

# Cache dependencies
COPY processor/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build JAR
COPY processor/src ./src
RUN mvn -q -DskipTests clean package
# Assembly plugin produces *-jar-with-dependencies.jar

# =========================
# STAGE 2: Install Node server deps
# =========================
FROM node:20-bookworm-slim AS node-builder
WORKDIR /build/server

COPY server/package*.json ./
RUN npm ci --omit=dev
COPY server ./

# =========================
# STAGE 3: Runtime (Java JRE + Node)
# =========================
FROM eclipse-temurin:24-jre

# Non-root user
RUN addgroup --system app && adduser --system --ingroup app app

# Bring Node runtime from node-builder (Debian-based)
COPY --from=node-builder /usr/local/bin/node /usr/local/bin/node
COPY --from=node-builder /usr/local/bin/corepack /usr/local/bin/corepack
COPY --from=node-builder /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm && \
    ln -s /usr/local/lib/node_modules/npm/bin/npx-cli.js /usr/local/bin/npx

WORKDIR /app

# Copy server (with its node_modules) and the fat JAR
COPY --from=node-builder /build/server /app/server
COPY --from=java-builder /build/processor/target/*-jar-with-dependencies.jar /app/processor/app.jar

# Environment and shared directories
ENV JAR_PATH=/app/processor/app.jar \
    VIDEOS_DIR=/videos \
    RESULTS_DIR=/results \
    NODE_ENV=production

RUN mkdir -p /videos /results && chown -R app:app /app /videos /results
USER app

# Adjust if your Node server listens on a different port
EXPOSE 3001
VOLUME ["/videos", "/results"]

WORKDIR /app/server
CMD ["npm", "start"]



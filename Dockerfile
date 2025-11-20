# =========================
# STAGE 1: Build the Java JAR (processor)
# =========================
FROM maven:3.9-eclipse-temurin-24-alpine AS java-builder
RUN apk update && apk upgrade
WORKDIR /build/processor
COPY processor/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY processor/src ./src
RUN mvn -q -DskipTests clean package && \
    mv target/*-jar-with-dependencies.jar target/app.jar

# =========================
# STAGE 2: Node.js Source Stage
# =========================
FROM node:20-alpine AS node-builder
RUN apk update && apk upgrade
WORKDIR /build/server
# Copy package files
COPY server/package*.json ./
# Copy all server source code 
COPY server/ . ./

# =========================
# STAGE 3: Runtime (Alpine JRE + Node)
# =========================
FROM eclipse-temurin:24-jre-alpine
RUN apk update && apk upgrade
# Add build tools AND ffmpeg system library
RUN apk add --no-cache nodejs npm python3 make g++ ffmpeg

RUN addgroup -S app && adduser -S -G app app
WORKDIR /app

# Copy server source (from node-builder) and the fat JAR (from java-builder)
COPY --from=node-builder /build/server /app/server
COPY --from=java-builder /build/processor/target/app.jar /app/processor/app.jar

# Set environment variables
ENV JAR_PATH=/app/processor/app.jar \
    VIDEOS_DIR=/videos \
    RESULTS_DIR=/results \
    NODE_ENV=production

# Create data directories
RUN mkdir -p /app/data /videos /results

# --- THIS IS THE CRITICAL FIX ---
WORKDIR /app/server
# 1. Delete any "leaked" node_modules from your host (e.g., Windows)
RUN rm -rf node_modules
# 2. Install 100% fresh, Linux-native modules
RUN npm ci --omit=dev
# ---------------------------------

# Set permissions for the whole app
RUN chown -R app:app /app /videos /results
USER app

EXPOSE 3000
VOLUME ["/videos", "/results", "/app/data"]

# run "node ./src/server.js"
CMD ["npm", "start"]
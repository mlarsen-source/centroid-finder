# =========================
# STAGE 1: Build the Application
# =========================
# We use a generic 'maven' image with a specific Java version (e.g., Java 21)
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set a working directory for the build process
WORKDIR /build

# 1. OPTIMIZATION: Copy only the pom.xml first.
# Docker will cache this layer. If you don't change your pom.xml,
# it won't re-download all your dependencies next time you build.
COPY pom.xml .

# Download all dependencies required for offline build.
# (This ensures the next step only needs your source code)
RUN mvn dependency:go-offline

# 2. Copy the rest of your actual source code
COPY src ./src

# 3. Build the application
# 'clean package' creates the .jar file in the /target directory.
# '-DskipTests' speeds up the build by not running unit tests in Docker.
RUN mvn clean package -DskipTests

# =========================
# STAGE 2: Run the Application
# =========================
# Use a smaller Java Runtime (JRE) image for the final container.
# This image DOES NOT have Maven or the full JDK, making it safer and smaller.
FROM eclipse-temurin:21-jre

# Set the working directory for the running app
WORKDIR /app

# Create a non-root user for security reasons (best practice)
RUN addgroup --system javauser && adduser --system --ingroup javauser javauser
USER javauser

# 4. Copy ONLY the built JAR file from the 'builder' stage above.
# We use a wildcard *.jar because the version name might change.
# We rename it to 'app.jar' for simplicity in the final command.
COPY --from=builder /build/target/*.jar app.jar

# Inform Docker that the container listens on this port at runtime.
# (Change 8080 if your app uses a different port)
EXPOSE 8080

# 5. The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
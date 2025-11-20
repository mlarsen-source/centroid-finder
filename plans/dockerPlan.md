# Centroid Finder Docker Image Plan

## Dockerfile Build Strategy: Multi-Stage Hybrid Image

### Stage 1 – Build Java Processor

**Base Image:**

- maven:3.9-eclipse-temurin-24-alpine
- lightweight Alpine image that includes Apache Maven 3.9 and Java 24 JDK (Temurin).

**Purpose:**

- compile the Java processor source code into an executable JAR file for use at runtime.

### Stage 2 – Build Express Server

**Base Image:**

- node:20-alpine
- lightweight Alpine image that provides the Node.js 20 runtime environment.

**Purpose:**

- copy the Node.js server source code and install production dependencies.

### Stage 3 – Final Runtime Image

**Base Image:**

- eclipse-temurin:24-jre-alpine
- minimal Java 24 JRE image for running the compiled processor JAR.

**Purpose:**

- Install Node.js and npm.
- Copy the built JAR and server files from the earlier stages.
- Define environment variables and working directories.
- Launch the Node.js server.

## Considerations

### What base docker image will you use?

- multi-stage build with several images
- use temporary images to compile JAR and copy server files
- final lightweight and secure base image for runtime

### How will you make sure that both node and Java can run in your image?

- final image includes the Java Runtime Environment and adds Node.js via the Alpine package manager

### How will you test your Dockerfile and image?

- test the image locally before publishing
- run docker build -t centroid-finder to ensure it builds successfully
- run the complete docker run command, including the -p and -v flags
- use Postman to validate requests and responses

### How will you make sure the endpoints are available outside the image?

- the Dockerfile will use EXPOSE 3000 to document the internal port used by the app.
- using the -p 3000:3000 flag will map the container port to the host port

### How will your code know where to access the video/results directory?

- use ENV VIDEOS_DIR=/videos inside the Dockerfile to tell our Node.js app to look for files in an internal folder named /videos.
- using the -v "$VIDEO_DIRECTORY:/videos" flag to link the external folder ($VIDEO_DIRECTORY) to the internal /videos folder.

### How can you make your docker image small, cacheable, and quick to make changes to?

- use lightweight alpine images and a .dockerignore file to avoid copying unnecessary files.
- use multi-stage build to use and then get rid of various build tools so only the JAR file and JRE are needed in the final image.
- copy pom.xml and package.json and run their install commands before copying the rest of the source code so that Docker only re-installs dependencies if those files change.

# Centroid Finder

A video analysis tool for tracking salamander movement in research videos. The system processes MP4 videos to detect and track colored markers, generating CSV data with timestamped centroid coordinates for behavioral analysis.

**Components:**

- **Processor** — Java/Maven library for video analysis and centroid detection
- **Server** — Node.js/Express API for job management, thumbnail generation, and result access

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Usage Examples](#usage-examples)
- [Project Structure](#project-structure)
- [Development](#development)

---

## Features

**Video Processing**

- Processes MP4 videos frame-by-frame using a Java backend.
- Converts frames into pixel arrays and performs Euclidean color-distance calculations.
- Binarizes frames based on user-selected color and threshold values.
- Identifies connected pixel groups using a BFS search algorithm.

**Centroid Detection**

- Computes the centroid (X, Y) of the largest detected pixel group within a frame.
- Records the timestamp of the frame associated with each centroid.

**CSV Output Generation**

- Produces a consolidated CSV file containing the per-frame centroid location and timestamp data.

**Executable Java Component**

- Packaged as a standalone executable JAR using Maven Assembly.
- Validates command-line arguments including video path, output path, hex color, and threshold.
- Provides clear error messages for invalid inputs and file issues.

**Express Server Integration**

- Node/Express server triggers the Java processor and manages job execution.
- Generates video thumbnails using ffmpeg for front-end previews.
- Provides endpoints to:
  - Create new processing jobs.
  - Retrieve job status.
  - Fetch generated video thumbnails.
  - List all available videos ready for processing.
  - List all generated CSV results.
  - Download a selected CSV result file.

**SQLite Data Management**

- Uses SQLite database to store job metadata.
- Tracks each job using:
  - `jobId` — unique identifier passed to clients.
  - `status` — "processing", "done", or "error".
  - `outputPath` — path to the generated CSV file.
- Schema is automatically synchronized at server startup using Sequelize.

**Containerized Deployment**

- Dockerized ecosystem including the Java processor, Express server, and SQLite storage.
- SQLite database file stored on a persistent volume.
- Shared volumes provide access to:
  - `/videos` — directory for available input videos.
  - `/results` — directory storing all generated CSV files.

**Comprehensive Testing**

- Thorough JUnit tests cover all Java classes and behaviors.
- Mocha/Chai tests validate all Express server logic and API endpoints.
- Esmock and Sinon provide mocking and stubbing for isolated server-side testing.

---

## Architecture

```
    Frontend Application (separate)
      ↓ (HTTP API calls)
    Express Server (port 3000)
    ├── Job Management Logic
    ├── Job Storage (SQLite)
    ├── Thumbnail Generation (FFmpeg)
    └── Spawns Java Processor
      ↓ (detached child process)
    Java Video Processor
    ├── Frame Extraction (JCodec)
    ├── Color Distance Calculation
    ├── Image Binarization
    ├── Pixel Group Detection (BFS)
    └── Centroid Calculation
      ↓ (writes CSV)
    Results Directory
```

**Processing Flow**

1. **Client Request**

   - Sends `POST /process/:fileName` with:
     - `targetColor` (hex, e.g., FFA200)
     - `threshold` (non-negative integer)

2. **Server Job Creation**

   - Generates a UUID-based job ID.
   - Inserts a job record into SQLite with status "processing"
   - Spawns the Java processor as a detached child process.
   - Returns the job ID for client-side status polling.

3. **Java Video Processing**

   - Extracts frame count and duration using JCodec; computes FPS.
   - Converts each frame to a `BufferedImage`.
   - Computes Euclidean RGB distance for each pixel.
   - Converts `BufferedImage` into a binary array.
   - Uses BFS algorithm to detect connected groups.
   - Sorts groups by size; selects the largest.
   - Computes centroid (x, y) of the largest group.
   - Generates timestamp (frameNumber / fps).

4. **CSV Output**

   - Writes results to `/results/:fileName.csv`.

5. **Job Completion**

   - Updates SQLite job status to "done" or "error".

6. **Client Retrieval**
   - Polls `/process/:jobId/status` until completed.
   - Downloads CSV from `/api/csv/:fileName`.

**Technology Stack**

- **Backend**: Node.js, Express, Sequelize, SQLite
- **Processor**: Java 24, Maven, JCodec
- **Media Tools**: FFmpeg
- **Testing**: JUnit, Mocha, Chai, Sinon, Esmock
- **Containerization**: Docker multi-stage build (Maven -> Node -> Eclipse-Temurin JRE)

---

## Prerequisites

---

## Quick Start

---

## API Documentation

---

## Project Structure

---

## Development

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

### Docker Deployment

- **Docker Engine installed**

### Local Development Environment

- **Java**: JDK 24+
- **Maven**: 4.0.0+
- **Node.js**: 24.11.1+
- **NPM**:11.6.2+

---

## Quick Start

### Docker

1.  **Build the image**

    ```
    docker build . -t centroid-finder
    ```

2.  **Run the container**

    ```
    docker run -p 3000:3000 -v "/path/to/local/videos:/videos" -v "/path/to/local/results:/results"
    centroid-finder
    ```

### Local Development

**Processor (Java)**

1.  **Enter the processor directory**

    ```
    cd processor
    ```

2.  **Build the JAR**

    ```
    mvn clean package
    ```

3.  **Run the processor manually**

    ```
    java -jar target/centroid-finder-1.0.0-jar-with-dependencies.jar  sampleInput/salamander_video.mp4  processor/results/output.csv  FFA200  164
    ```

    **Processor Arguments**

    1. Input video path
    2. Output CSV path
    3. Target color
    4. Threshold

**Server (Node.js)**

1.  **Enter the server directory**

    ```
    cd server
    ```

2.  **Install dependencies**

    ```
    npm install
    ```

3.  **Start the server**

    ```
    npm start
    ```

Server will be available at: **http://localhost:3000**

---

## API Documentation

### Endpoints

#### `GET /api/videos`

List all video files available for processing.

**Response:**

```json
["salamander_video1.mp4", "salamander_video2.mp4", "salamander_video3.mp4"]
```

---

#### `GET /api/results`

List all processed CSV result files.

**Response:**

```json
["salamander_video1.csv", "salamander_video2.csv", "salamander_video3.csv"]
```

---

#### `GET /api/csv/:fileName`

Download a specific CSV result file.

**Parameters:**

- `fileName` (path): Name of the CSV file

**Response:**

- Content-Type: `text/csv`
- Content-Disposition: `attachment`

**Example:**

```
http://localhost:3000/api/csv/salamander_video.mp4.csv
```

---

#### `GET /thumbnail/:fileName`

Generate and retrieve a thumbnail image (first frame) from a video.

**Parameters:**

- `fileName` (path): Name of the video file

**Response:**

- Content-Type: `image/jpeg`

**Example:**

```
http://localhost:3000/thumbnail/salamander_video.mp4
```

---

#### `POST /process/:fileName`

Start a video processing job.

**Parameters:**

- `fileName` (path): Name of the video file in `/videos` directory

**Query Parameters:**

- `targetColor` (required): 6-character hex color code (e.g., `FFA200`)
- `threshold` (required): Integer threshold for color matching (e.g., `164`)

**Response:**

```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Example:**

```bash
POST http://localhost:3000/process/salamander_video.mp4?targetColor=FFA200&threshold=164
```

---

#### `GET /process/:jobId/status`

Check the status of a processing job.

**Parameters:**

- `jobId` (path): UUID returned from the POST /process endpoint

**Response:**

**Processing:**

```json
{
  "status": "processing"
}
```

**Completed:**

```json
{
  "status": "done",
  "outputPath": "/results/salamander_video.mp4.csv"
}
```

**Error:**

```json
{
  "status": "error",
  "error": "Error processing video: Unexpected ffmpeg error"
}
```

---

## Project Structure

```
centroid-finder/
|
├── plans/                     # Documentation and development notes
|
├── processor/                 # Java Maven processor
│   ├── src/
│   │   ├── main/java/io/github/mlarsen_source/centroid_finder/
│   │   │   ├── VideoProcessingApp.java              # Main entry point
│   │   │   ├── VideoProcessingAppRunner.java        # Processing coordinator
│   │   │   ├── ArgumentParser.java                  # Interface for argument parsing
│   │   │   ├── CommandLineParser.java               # CLI argument parser/validator
│   │   │   ├── VideoProcessor.java                  # Interface for video operations
│   │   │   ├── Mp4VideoProcessor.java               # MP4 frame extraction & FPS calculation
│   │   │   ├── VideoGroupFinder.java                # Interface for video analysis
│   │   │   ├── Mp4VideoGroupFinder.java             # Frame-by-frame centroid extraction
|   |   |   ├── ColorDistanceFinder.java             # Interface for color distance
│   │   │   ├── EuclideanColorDistance.java          # RGB Euclidean distance calculator
│   │   │   ├── ImageBinarizer.java                  # Interface for image binarization
│   │   │   ├── DistanceImageBinarizer.java          # Color distance-based binarization
│   │   │   ├── ImageGroupFinder.java                # Interface for connected components
│   │   │   ├── BinarizingImageGroupFinder.java      # Binarize + find groups pipeline
│   │   │   ├── BinaryGroupFinder.java               # Interface for binary image groups
│   │   │   ├── BfsBinaryGroupFinder.java            # BFS connected group detection
│   │   │   ├── DataWriter.java                      # Interface for output writing
│   │   │   ├── CsvWriter.java                       # CSV file writer
│   │   │   ├── Coordinate.java                      # Record: (x, y) position
│   │   │   ├── Group.java                           # Record: pixel group with centroid
│   │   │   ├── TimedCoordinate.java                 # Record: centroid + timestamp
│   │   │   └── FrameData.java                       # Record: video metadata
|   |   |
│   │   └── test/java/         # JUnit tests
│   │
│   ├── diagrams/              # Processor architecture diagrams
│   ├── sampleInput/           # Example videos for testing
│   ├── sampleOutput/          # Example CSV outputs for testing
│   └── pom.xml                # Maven configuration
│
├── server/                    # Node.js Express server
│   ├── src/
│   │   ├── server.js          # Express app initialization
│   │   ├── routes/
│   │   │   └── routes.js      # API endpoint definitions
│   │   ├── controllers/
│   │   │   └── controller.js  # Request handlers, processor spawning
│   │   ├── repos/
│   │   │   └── repos.js       # SQLite job repository
│   │   ├── db/
│   │   │   └── connect.js     # Sequelize connection setup
│   │   ├── models/
│   │   │   └── models.js      # Job table schema
│   │   └── tests/             # Mocha/Chai server tests
│   │
│   ├── diagrams/              # Server architecture diagrams
│   └── package.json           # Node dependencies
│
├── .gitignore                 # Git ignore rules
├── .dockerignore              # Docker ignore rules
├── Dockerfile                 # Multi-stage build
└── README.md
```

---

## Development

### Running Tests

**Processor (Java):**

```
cd processor
mvn test
```

**Server (Node):**

```
cd server
npm test
```

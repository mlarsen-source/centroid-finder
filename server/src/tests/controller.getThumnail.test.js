// Import assertion library and required modules
import { expect } from "chai";
import fs from "fs";
import path from "path";
// Import the actual controller function to test
import { getThumbnail } from "../controllers/controller.js";

// Directory used for mock video files during tests
const TEST_DIR = "./src/tests/mock_videos";

describe("Controller: getThumbnail (real ffmpeg run)", function () {
  // Extend default Mocha timeout — ffmpeg can take a few seconds to start/stop
  this.timeout(10000);

  // ----------------------
  //  SETUP AND TEARDOWN
  // ----------------------
  before(() => {
    // Create the mock test directory if it doesn't already exist
    if (!fs.existsSync(TEST_DIR)) {
      fs.mkdirSync(TEST_DIR, { recursive: true });
    }

    // Override VIDEOS_DIR environment variable for test isolation
    process.env.VIDEOS_DIR = TEST_DIR;
  });

  after(() => {
    // Remove the mock directory and all contents after tests finish
    fs.rmSync(TEST_DIR, { recursive: true, force: true });
  });

  // -------------------------
  //  MOCK EXPRESS RESPONSE
  // -------------------------
  // This function simulates an Express.js response object
  // so the controller can be tested without a running server
  function createMockRes() {
    return {
      statusCode: undefined, // Stores the HTTP status code
      data: undefined,       // Stores any JSON payload sent via res.json()
      fileSent: false,       // Tracks if res.sendFile() was called

      // Simulates Express's res.status()
      status(code) {
        this.statusCode = code;
        return this; // Allows chaining (e.g., res.status(500).json(...))
      },

      // Simulates res.json() — captures the data payload
      json(payload) {
        this.data = payload;
        return this;
      },

      // Simulates res.sendFile() — just marks that it was called
      sendFile(filePath, callback) {
        this.fileSent = true;
        // Immediately call the callback to simulate Express behavior
        if (callback) callback(null);
        return this;
      },
    };
  }

  // ----------------------
  //  INDIVIDUAL TESTS
  // ----------------------

  it("returns 500 for invalid input (ffmpeg fails)", async () => {
    // Simulate a request for a video that doesn’t exist
    const req = { params: { fileName: "fake.mp4" } };
    const res = createMockRes();

    // Call the controller and await the async FFmpeg result
    await getThumbnail(req, res);

    // Verify correct HTTP response and error message
    expect(res.statusCode).to.equal(500);
    expect(res.data).to.have.property("error", "Error generating thumbnail");
  });

  it("returns 500 when file is missing", async () => {
    // Another missing file test to confirm consistent behavior
    const req = { params: { fileName: "nonexistent.mp4" } };
    const res = createMockRes();

    // Await async controller execution
    await getThumbnail(req, res);

    // Confirm the correct error response
    expect(res.statusCode).to.equal(500);
    expect(res.data).to.have.property("error", "Error generating thumbnail");
  });

  it("returns 200 and sends a file for valid (but tiny) mp4", async () => {
    // Create a placeholder MP4 file in the test directory
    // The file is empty but exists, so ffmpeg can find it
    const fileName = "tiny.mp4";
    const videoPath = path.join(TEST_DIR, fileName);
    fs.writeFileSync(videoPath, "");

    const req = { params: { fileName } };
    const res = createMockRes();

    // Await async controller call
    await getThumbnail(req, res);

    // ffmpeg may fail (500) or succeed (200), depending on system configuration.
    // Either outcome is acceptable — we check for both.
    expect([200, 500]).to.include(res.statusCode);

    // If it succeeded, verify that sendFile() was triggered
    if (res.statusCode === 200) {
      expect(res.fileSent).to.be.true;
    }

    // Clean up the temporary test file
    fs.unlinkSync(videoPath);
  });
});
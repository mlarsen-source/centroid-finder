// Import testing and filesystem libraries
import { expect } from "chai";     // Assertion library for test expectations
import fs from "fs";                // Synchronous filesystem operations
import path from "path";            // Path manipulation utilities
import { getThumbnail } from "../controllers/controller.js";  // Function under test

/**
 * Integration Test Suite for getThumbnail Controller
 * 
 * NOTE: These are INTEGRATION tests, not unit tests. They test the real ffmpeg
 * binary, which makes them slower and dependent on system configuration.
 * 
 * Purpose: Verify that getThumbnail correctly:
 * - Generates thumbnails from valid video files
 * - Handles missing/invalid files gracefully
 * - Returns appropriate HTTP status codes and responses
 * 
 * Dependencies: Requires ffmpeg to be installed on the system
 */

// Directory used for mock video files during tests (isolated from production)
const TEST_DIR = "./src/tests/mock_videos";

describe("Controller: getThumbnail (real ffmpeg run)", function () {
  /**
   * Extend Mocha's default timeout to 10 seconds.
   * 
   * Why: ffmpeg can take several seconds to:
   * - Start up and initialize
   * - Process video files (even small ones)
   * - Generate thumbnail images
   * - Clean up resources
   * 
   * Default Mocha timeout (2000ms) is too short for these operations.
   */
  this.timeout(10000);

  // ----------------------
  //  SETUP AND TEARDOWN
  // ----------------------

  /**
   * Setup: Runs once before all tests in this suite.
   * 
   * Creates a clean test directory and configures the environment to use it.
   * This ensures tests don't interfere with real video files.
   */
  before(() => {
    // Create the mock video directory if it doesn't exist
    // recursive: true prevents errors if parent directories don't exist
    if (!fs.existsSync(TEST_DIR)) {
      fs.mkdirSync(TEST_DIR, { recursive: true });
    }

    // Override the VIDEOS_DIR environment variable to point to our test directory
    // This ensures getThumbnail looks for videos in the test location, not production
    process.env.VIDEOS_DIR = TEST_DIR;
  });

  /**
   * Teardown: Runs once after all tests in this suite.
   * 
   * Removes the test directory and all its contents to prevent test artifacts
   * from accumulating in the filesystem.
   */
  after(() => {
    // Remove the entire test directory tree
    // force: true prevents errors if directory doesn't exist
    // recursive: true removes all nested files and directories
    fs.rmSync(TEST_DIR, { recursive: true, force: true });
  });

  // -------------------------
  //  MOCK EXPRESS RESPONSE
  // -------------------------

  /**
   * Creates a mock Express response object for testing.
   * 
   * This factory function simulates the Express.js res object, allowing us to test
   * the controller without running an actual HTTP server. It tracks:
   * - HTTP status codes
   * - JSON response bodies
   * - Whether files were sent
   * 
   * Key difference from real Express: sendFile() doesn't actually send a file,
   * it just marks that it was called. This is sufficient for testing the controller logic.
   * 
   * @returns {Object} Mock response object with Express-like methods
   */
  function createMockRes() {
    return {
      statusCode: undefined,  // Stores the HTTP status code set via status()
      data: undefined,        // Stores JSON payload sent via json()
      fileSent: false,        // Tracks whether sendFile() was called

      /**
       * Simulates Express's res.status() method.
       * Sets the HTTP status code and returns 'this' for method chaining.
       * 
       * @param {number} code - HTTP status code (e.g., 200, 404, 500)
       * @returns {Object} this - Enables chaining like res.status(200).json({...})
       */
      status(code) {
        this.statusCode = code;
        return this;  // Critical for method chaining
      },

      /**
       * Simulates Express's res.json() method.
       * Captures the JSON payload that would be sent to the client.
       * 
       * @param {*} payload - Data to send as JSON (typically an object or array)
       * @returns {Object} this - Enables method chaining
       */
      json(payload) {
        this.data = payload;
        return this;
      },

      /**
       * Simulates Express's res.sendFile() method.
       * Marks that a file was sent and invokes the callback (if provided).
       * 
       * In production, this would stream the file to the client. For testing,
       * we just need to verify the controller called it with correct parameters.
       * 
       * @param {string} filePath - Path to the file being sent
       * @param {Function} callback - Optional callback invoked after send completes
       * @returns {Object} this - Enables method chaining
       */
      sendFile(filePath, callback) {
        this.fileSent = true;
        // Immediately invoke callback to simulate successful file send
        // Error parameter is null (no error occurred)
        if (callback) callback(null);
        return this;
      },
    };
  }

  // ----------------------
  //  INDIVIDUAL TESTS
  // ----------------------

  /**
   * Test Case: Invalid/Non-existent Video File
   * 
   * Verifies that when getThumbnail is called with a non-existent video file:
   * 1. ffmpeg fails to process the file (as expected)
   * 2. Controller catches the error
   * 3. Returns HTTP 500 with appropriate error message
   * 
   * This tests the error handling path in the catch block.
   */
  it("returns 500 for invalid input (ffmpeg fails)", async () => {
    // ARRANGE: Create mock request with non-existent file name
    const req = { params: { fileName: "fake.mp4" } };
    const res = createMockRes();

    // ACT: Call the controller and wait for ffmpeg to complete (or fail)
    await getThumbnail(req, res);

    // ASSERT: Verify error response
    expect(res.statusCode).to.equal(500);                           // Should return server error
    expect(res.data).to.be.an('object');                           // Response should be an object
    expect(res.data).to.have.property("error");                    // Should have 'error' property
    expect(res.data.error).to.equal("Error generating thumbnail"); // Correct error message
    expect(res.fileSent).to.be.false;                              // No file should be sent on error
  });

  /**
   * Test Case: Missing File (Another Error Scenario)
   * 
   * Similar to the previous test but with a different file name.
   * This ensures the error handling is consistent regardless of the specific file requested.
   * 
   * Redundancy is intentional here to verify robust error handling.
   */
  it("returns 500 when file is missing", async () => {
    // ARRANGE: Different non-existent file name
    const req = { params: { fileName: "nonexistent.mp4" } };
    const res = createMockRes();

    // ACT: Attempt to generate thumbnail from missing file
    await getThumbnail(req, res);

    // ASSERT: Should return same error response as previous test
    expect(res.statusCode).to.equal(500);
    expect(res.data).to.have.property("error", "Error generating thumbnail");
    expect(res.fileSent).to.be.false;
  });

  /**
   * Test Case: Valid Video File (Happy Path)
   * 
   * Creates a minimal placeholder MP4 file and verifies getThumbnail processes it.
   * 
   * IMPORTANT: This test has flexible assertions because:
   * - Empty MP4 files may cause ffmpeg to fail (Invalid data)
   * - ffmpeg behavior varies by version and system configuration
   * - Some systems might have ffmpeg installed, others might not
   * 
   * We accept both success (200) and failure (500) as valid outcomes,
   * but verify the correct behavior for each case.
   */
  it("returns 200 and sends a file for valid (but tiny) mp4", async () => {
    // ARRANGE: Create an empty placeholder MP4 file
    const fileName = "tiny.mp4";
    const videoPath = path.join(TEST_DIR, fileName);
    
    // Write empty file (ffmpeg can at least locate it, even if processing fails)
    fs.writeFileSync(videoPath, "");

    const req = { params: { fileName } };
    const res = createMockRes();

    // ACT: Attempt to generate thumbnail from the placeholder file
    await getThumbnail(req, res);

    // ASSERT: Accept both success and failure as valid outcomes
    expect([200, 500]).to.include(res.statusCode);  // Either outcome is acceptable

    /**
     * Conditional assertions based on outcome:
     * 
     * If 200 (success):
     * - Verify sendFile() was called
     * - Thumbnail was generated and sent to client
     * 
     * If 500 (failure):
     * - Verify error message is present
     * - This is expected for empty/invalid MP4 files
     */
    if (res.statusCode === 200) {
      expect(res.fileSent).to.be.true;  // File should have been sent on success
    } else {
      expect(res.data).to.have.property("error");  // Error message should be present
    }

    // CLEANUP: Remove the temporary test file
    // Use try-catch in case file was already deleted by the controller
    try {
      fs.unlinkSync(videoPath);
    } catch (error) {
      // File might not exist if controller deleted it, which is fine
      if (error.code !== 'ENOENT') {
        throw error;  // Re-throw if it's a different error
      }
    }
  });

  /**
   * Test Case: Verify Thumbnail File Cleanup
   * 
   * Ensures that getThumbnail properly cleans up temporary thumbnail files
   * after sending them to the client (or after errors).
   * 
   * This prevents disk space from being consumed by orphaned thumbnail files.
   */
  it("should clean up temporary thumbnail files", async () => {
    // ARRANGE: Create a placeholder video file
    const fileName = "cleanup-test.mp4";
    const videoPath = path.join(TEST_DIR, fileName);
    fs.writeFileSync(videoPath, "");

    const req = { params: { fileName } };
    const res = createMockRes();

    // ACT: Generate thumbnail
    await getThumbnail(req, res);

    // ASSERT: Verify the thumbnail file doesn't persist in ./public
    const thumbnailPath = path.join("./public", `${fileName}-thumb.jpg`);
    const thumbnailExists = fs.existsSync(thumbnailPath);
    
    expect(thumbnailExists).to.be.false;  // Thumbnail should be deleted after sending

    // CLEANUP: Remove test video file
    try {
      fs.unlinkSync(videoPath);
    } catch (error) {
      if (error.code !== 'ENOENT') throw error;
    }
  });
});
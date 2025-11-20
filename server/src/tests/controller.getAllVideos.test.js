// Import testing and filesystem libraries
import { expect } from "chai";           // Assertion library for test expectations
import fs from "fs/promises";            // Promise-based filesystem operations
import path from "path";                 // Path manipulation utilities
import { getAllVideos } from "../controllers/controller.js";  // Function under test

/**
 * Creates a mock Express response object for testing.
 * 
 * This factory function returns an object that mimics Express.js res behavior,
 * storing the status code and JSON data internally so we can assert against them.
 * The methods support chaining (returning 'this') just like Express.
 * 
 * @returns {Object} Mock response object with status() and json() methods
 */
function createMockRes() {
  return {
    statusCode: null,    // Stores the HTTP status code
    jsonData: null,      // Stores the JSON response body
    
    // Sets the HTTP status code and returns 'this' for method chaining
    status(code) {
      this.statusCode = code;
      return this;  // Enables res.status(200).json({...}) pattern
    },
    
    // Sets the JSON response body and returns 'this' for method chaining
    json(data) {
      this.jsonData = data;
      return this;
    },
  };
}

/**
 * Test suite for the getAllVideos controller function.
 * 
 * Purpose: Verify that getAllVideos correctly reads video files from a directory
 * and handles both success and error cases appropriately.
 * 
 * Test Strategy: Use a temporary directory to isolate tests from the real filesystem,
 * ensuring tests don't interfere with actual video files or each other.
 */
describe("UNIT: getAllVideos()", () => {
  // Define the temporary test directory path (will be created/destroyed for tests)
  const tempDir = path.join(process.cwd(), "src", "tests", "temp_videos");

  /**
   * Setup: Runs once before all tests in this suite.
   * 
   * Creates a clean temporary directory and configures the environment
   * to use it. This ensures tests run in isolation from production data.
   */
  before(async () => {
    // Create the directory if it doesn't exist (recursive: true prevents errors)
    await fs.mkdir(tempDir, { recursive: true });
    
    // Point the controller to our test directory instead of the real one
    process.env.VIDEOS_DIR = tempDir;
  });

  /**
   * Teardown: Runs once after all tests in this suite.
   * 
   * Cleans up the temporary directory by removing all files and the directory itself.
   * This prevents test artifacts from accumulating in the filesystem.
   */
  after(async () => {
    try {
      // Read all files in the temp directory
      const files = await fs.readdir(tempDir);
      
      // Delete each file individually
      for (const f of files) {
        await fs.unlink(path.join(tempDir, f));
      }
      
      // Remove the now-empty directory
      await fs.rmdir(tempDir);
    } catch (error) {
      // If cleanup fails (e.g., directory already deleted), log but don't fail tests
      console.warn(`Cleanup warning: ${error.message}`);
    }
  });

  /**
   * Test Case: Success scenario with files present
   * 
   * Verifies that when the video directory contains files, getAllVideos:
   * 1. Returns HTTP 200 status
   * 2. Returns an array containing the file names
   */
  it("should return 200 and a list of videos when directory has files", async () => {
    // ARRANGE: Create a test video file in the temporary directory
    const sampleFile = path.join(tempDir, "video1.mp4");
    await fs.writeFile(sampleFile, "fake content");  // Content doesn't matter for this test

    // Create a mock response object to capture the controller's output
    const res = createMockRes();
    
    // Create an empty request object (getAllVideos doesn't use req)
    const req = {};

    // ACT: Call the controller function with our mock objects
    await getAllVideos(req, res);

    // ASSERT: Verify the response is correct
    expect(res.statusCode).to.equal(200);           // Should return success status
    expect(res.jsonData).to.be.an('array');         // Response should be an array
    expect(res.jsonData).to.include("video1.mp4");  // Array should contain our test file
    expect(res.jsonData).to.have.lengthOf(1);       // Should only have 1 file
  });

  /**
   * Test Case: Error scenario with non-existent directory
   * 
   * Verifies that when the video directory doesn't exist or can't be read:
   * 1. Returns HTTP 500 status (server error)
   * 2. Returns an error object with descriptive message
   * 
   * This tests the error handling path in the try-catch block.
   */
  it("should return 500 when the directory does not exist", async () => {
    // ARRANGE: Save the current directory and temporarily point to invalid path
    const oldDir = process.env.VIDEOS_DIR;
    process.env.VIDEOS_DIR = path.join(tempDir, "does_not_exist");

    // Create mock response object
    const res = createMockRes();
    const req = {};

    // ACT: Call controller with invalid directory path
    await getAllVideos(req, res);

    // ASSERT: Verify error response
    expect(res.statusCode).to.equal(500);                    // Should return server error
    expect(res.jsonData).to.be.an('object');                 // Should return an object
    expect(res.jsonData).to.have.property("error");          // Should have 'error' property
    expect(res.jsonData.error).to.equal("Error reading video directory");  // Correct error message

    // CLEANUP: Restore the original directory path for subsequent tests
    // This is critical - without it, other tests would fail
    process.env.VIDEOS_DIR = oldDir;
  });

  /**
   * Test Case: Empty directory scenario
   * 
   * Verifies that when the directory exists but contains no files:
   * 1. Returns HTTP 200 status (empty is still success)
   * 2. Returns an empty array
   */
  it("should return 200 and an empty array when directory has no files", async () => {
    // ARRANGE: Ensure temp directory is empty by removing any leftover files
    const files = await fs.readdir(tempDir);
    for (const f of files) {
      await fs.unlink(path.join(tempDir, f));
    }

    const res = createMockRes();
    const req = {};

    // ACT: Call controller with empty directory
    await getAllVideos(req, res);

    // ASSERT: Should succeed with empty array
    expect(res.statusCode).to.equal(200);
    expect(res.jsonData).to.be.an('array');
    expect(res.jsonData).to.have.lengthOf(0);  // Empty array
  });

  /**
   * Test Case: Multiple files scenario
   * 
   * Verifies that getAllVideos correctly returns all files when multiple exist.
   */
  it("should return all video files when multiple exist", async () => {
    // ARRANGE: Create multiple test files
    const files = ["video1.mp4", "video2.mp4", "video3.avi"];
    for (const filename of files) {
      await fs.writeFile(path.join(tempDir, filename), "test content");
    }

    const res = createMockRes();
    const req = {};

    // ACT: Call controller
    await getAllVideos(req, res);

    // ASSERT: Should return all files
    expect(res.statusCode).to.equal(200);
    expect(res.jsonData).to.have.lengthOf(3);
    expect(res.jsonData).to.include.members(files);  // Contains all expected files

    // CLEANUP: Remove test files for next test
    for (const filename of files) {
      await fs.unlink(path.join(tempDir, filename));
    }
  });
});
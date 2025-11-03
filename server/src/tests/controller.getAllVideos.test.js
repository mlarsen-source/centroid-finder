import { expect } from "chai";
import fs from "fs/promises";
import path from "path";
import { getAllVideos } from "../controllers/controller.js";

/**
 * Creates a mock response object that mimics the behavior of Express.js.
 * This allows the controller function to call res.status() and res.json()
 * without requiring an actual running server.
 */
function createMockRes() {
  return {
    statusCode: null,
    jsonData: null,
    status(code) {
      this.statusCode = code;
      return this;
    },
    json(data) {
      this.jsonData = data;
      return this;
    },
  };
}

/**
 * Test suite for the getAllVideos controller function.
 * These tests focus on verifying that the function handles both
 * successful and failing filesystem reads correctly.
 */
describe("UNIT: getAllVideos()", () => {
  // Define a path for a temporary folder used only during these tests
  const tempDir = path.join(process.cwd(), "src", "tests", "temp_videos");

  /**
   * before() runs once before all tests in this suite.
   * It ensures the temporary test directory exists and updates
   * the VIDEOS_DIR environment variable to point there.
   */
  before(async () => {
    await fs.mkdir(tempDir, { recursive: true });
    process.env.VIDEOS_DIR = tempDir;
  });

  /**
   * after() runs once after all tests in this suite.
   * It deletes any files created in the temporary directory,
   * then removes the directory itself to leave the workspace clean.
   */
  after(async () => {
    const files = await fs.readdir(tempDir);
    for (const f of files) {
      await fs.unlink(path.join(tempDir, f));
    }
    await fs.rmdir(tempDir);
  });

  /**
   * Test case: verifies that getAllVideos() responds with HTTP 200
   * and includes the expected file name when the directory read succeeds.
   */
  it("should return 200 and a list of videos when directory has files", async () => {
    // Arrange: create a dummy .mp4 file inside the test directory
    const sampleFile = path.join(tempDir, "video1.mp4");
    await fs.writeFile(sampleFile, "fake content");

    const res = createMockRes();

    // Act: call the controller with no request object (req is unused)
    await getAllVideos({}, res);

    // Assert: expect a successful response with the correct file name
    expect(res.statusCode).to.equal(200);
    expect(res.jsonData).to.include("video1.mp4");
  });

  /**
   * Test case: verifies that getAllVideos() returns HTTP 500 and an error object
   * when the target directory cannot be read (e.g., missing path).
   */
  it("should return 500 when the directory does not exist", async () => {
    // Temporarily change VIDEOS_DIR to an invalid path
    const oldDir = process.env.VIDEOS_DIR;
    process.env.VIDEOS_DIR = path.join(tempDir, "does_not_exist");

    const res = createMockRes();
    await getAllVideos({}, res);

    // Assert: expect a 500 error response with an 'error' field in JSON
    expect(res.statusCode).to.equal(500);
    expect(res.jsonData).to.have.property("error");

    // Restore the original VIDEOS_DIR value for subsequent tests
    process.env.VIDEOS_DIR = oldDir;
  });
});
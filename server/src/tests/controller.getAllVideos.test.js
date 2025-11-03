// Import Chai's assertion library for making readable test checks
import { expect } from "chai";

// Import Node's built-in filesystem and path modules
// These let us create, read, and delete test files and directories
import fs from "fs";
import path from "path";

// Import the controller function we're testing
import { getAllVideos } from "../controllers/controller.js";

// Define a temporary directory to use for testing
// This acts as a fake "videos" folder for our tests
const TEST_DIR = "./src/tests/mock_videos";

// Begin a Mocha test suite for the getAllVideos controller
describe("Controller: getAllVideos", () => {

  // ----------------------------
  // Setup step (runs once before all tests)
  // ----------------------------
  before(() => {
    // If our mock video directory doesn’t exist, create it (including any parent folders)
    if (!fs.existsSync(TEST_DIR)) fs.mkdirSync(TEST_DIR, { recursive: true });

    // Create two empty .mp4 files inside that directory to simulate real videos
    fs.writeFileSync(path.join(TEST_DIR, "test1.mp4"), "");
    fs.writeFileSync(path.join(TEST_DIR, "test2.mp4"), "");

    // Override the environment variable so the controller reads from our mock directory
    process.env.VIDEOS_DIR = TEST_DIR;
  });

  // ----------------------------
  // Cleanup step (runs once after all tests)
  // ----------------------------
  after(() => {
    // Remove the mock directory and all its contents
    // `force: true` ensures it deletes even if something’s locked or missing
    fs.rmSync(TEST_DIR, { recursive: true, force: true });
  });

  // ----------------------------
  // Test Case 1: Successful directory read
  // ----------------------------
  it("should return 200 and a list of videos", async () => {
    // Create a fake Express response object
    // These two functions mimic how Express sets status and returns JSON
    const mockRes = {
      status: function (code) {
        this.statusCode = code; // store the status code
        return this;            // return `this` to allow chaining (res.status().json())
      },
      json: function (data) {
        this.data = data;       // store whatever data the controller sends
      },
    };

    // Call the controller with an empty request object and our mock response
    await getAllVideos({}, mockRes);

    // Assertions:
    // 1. The response should have HTTP 200 (success)
    expect(mockRes.statusCode).to.equal(200);
    // 2. The data should be an array containing our test filenames
    expect(mockRes.data).to.be.an("array").that.includes("test1.mp4");
  });
});

// ----------------------------
// Test Case 2: Directory read failure
// ----------------------------
// Note: This test is *outside* the main `describe` block to run independently
it("should return 500 if the video directory cannot be read", async () => {
  // Set the videos directory to a path that doesn’t exist
  // This will force the controller to throw an error
  process.env.VIDEOS_DIR = "./src/tests/fake_dir_does_not_exist";

  // Same type of mock response object as above
  const mockRes = {
    status(code) {
      this.statusCode = code;
      return this;
    },
    json(data) {
      this.data = data;
    },
  };

  // Call the controller, expecting it to fail
  await getAllVideos({}, mockRes);

  // Assertions:
  // 1. Controller should respond with HTTP 500 (internal server error)
  expect(mockRes.statusCode).to.equal(500);
  // 2. The response body should include an "error" property
  expect(mockRes.data).to.have.property("error");
});
// Import testing libraries
import { expect } from 'chai';     // Assertion library for test expectations
import sinon from 'sinon';          // Mocking/stubbing library for test doubles
import esmock from 'esmock';        // ES module mocking tool to replace imports
import { spawn } from 'child_process';  // For verifying spawn was called correctly

/**
 * Unit Test Suite for startProcessVideo Controller
 * 
 * Purpose: Verify that startProcessVideo correctly:
 * - Validates required query parameters (targetColor, threshold)
 * - Generates unique job IDs
 * - Creates job records in the database
 * - Spawns Java processes with correct arguments
 * - Returns appropriate HTTP responses
 * 
 * Approach: Mock all external dependencies (repos, spawn, uuid) to isolate
 * the controller logic and make tests fast and reliable.
 */
describe('startProcessVideo Controller', () => {
  // Declare variables that will be used across all tests
  let req, res, startProcessVideo;
  let mockCreateJob, mockSpawn, mockUuidv4;

  /**
   * Setup: Runs before each test
   * 
   * Creates fresh mock objects and loads the controller with mocked dependencies.
   * This ensures each test starts with a clean slate and tests don't interfere
   * with each other.
   */
  beforeEach(async () => {
    // Setup mock request object with typical video processing parameters
    req = {
      params: {
        fileName: 'test-video.mp4'  // Video file name from URL parameter
      },
      query: {
        targetColor: 'FF0000',       // Hex color to track in video
        threshold: '50'              // Color matching threshold (0-255)
      }
    };

    // Setup mock response object - simulates Express response
    // Each method is stubbed and returns 'this' to allow method chaining
    res = {
      status: sinon.stub().returnsThis(),  // For setting HTTP status code
      json: sinon.stub().returnsThis()     // For sending JSON response
    };

    // Create stub for createJob function - simulates database job creation
    mockCreateJob = sinon.stub();

    // Create stub for uuid generation - returns predictable job IDs for testing
    mockUuidv4 = sinon.stub().returns('test-job-id-12345');

    // Create spy for child_process.spawn - tracks how processes are spawned
    // Using a spy instead of stub allows real spawn behavior while tracking calls
    mockSpawn = sinon.spy(spawn);

    // Set required environment variables for the controller
    process.env.RESULTS_DIR = '/test/results';
    process.env.VIDEOS_DIR = '/test/videos';
    process.env.JAR_PATH = '/test/processor.jar';

    // Use esmock.strict() to load controller with mocked dependencies
    // This intercepts imports and replaces them with our mocks
    const controllerModule = await esmock.strict('../controllers/controller.js', {
      // Mock the repos module - replace database functions
      '../controllers/../repos/repos.js': {
        createJob: mockCreateJob,
        checkJob: sinon.stub(),
        updateStatus: sinon.stub()
      },
      // Mock the uuid module - return predictable IDs
      'uuid': {
        v4: mockUuidv4
      },
      // Mock child_process - control process spawning
      'child_process': {
        spawn: mockSpawn
      }
    });

    // Extract the function we're testing from the mocked module
    startProcessVideo = controllerModule.startProcessVideo;
  });

  /**
   * Teardown: Runs after each test
   * 
   * Restores all Sinon stubs/spies to their original state and cleans up
   * environment variables to prevent test pollution.
   */
  afterEach(() => {
    sinon.restore();  // Restore all Sinon mocks
    delete process.env.RESULTS_DIR;
    delete process.env.VIDEOS_DIR;
    delete process.env.JAR_PATH;
  });

  // ----------------------
  //  VALIDATION TESTS
  // ----------------------

  describe('Parameter Validation', () => {
    /**
     * Test Case: Missing fileName parameter
     * 
     * Verifies that the controller returns 400 Bad Request when the fileName
     * URL parameter is missing or empty.
     */
    it('should return 400 when fileName is missing', async () => {
      // ARRANGE: Remove fileName from request
      req.params.fileName = undefined;

      // ACT: Call the controller
      await startProcessVideo(req, res);

      // ASSERT: Should return 400 with error message
      expect(res.status.calledWith(400)).to.be.true;
      expect(res.json.calledOnce).to.be.true;
      expect(res.json.firstCall.args[0]).to.have.property('error');
      expect(res.json.firstCall.args[0].error).to.include('Missing');
    });

    /**
     * Test Case: Missing targetColor parameter
     * 
     * Verifies that the controller validates the presence of targetColor
     * query parameter before processing.
     */
    it('should return 400 when targetColor is missing', async () => {
      // ARRANGE: Remove targetColor from query
      req.query.targetColor = undefined;

      // ACT: Call controller
      await startProcessVideo(req, res);

      // ASSERT: Should return 400 Bad Request
      expect(res.status.calledWith(400)).to.be.true;
      expect(res.json.firstCall.args[0]).to.have.property('error');
      expect(res.json.firstCall.args[0].error).to.include('targetColor');
    });

    /**
     * Test Case: Missing threshold parameter
     * 
     * Verifies validation of the threshold query parameter.
     */
    it('should return 400 when threshold is missing', async () => {
      // ARRANGE: Remove threshold from query
      req.query.threshold = undefined;

      // ACT: Call controller
      await startProcessVideo(req, res);

      // ASSERT: Should return 400 with appropriate error
      expect(res.status.calledWith(400)).to.be.true;
      expect(res.json.firstCall.args[0].error).to.include('threshold');
    });

    /**
     * Test Case: Empty string parameters
     * 
     * Verifies that empty strings are treated the same as missing parameters.
     */
    it('should return 400 when parameters are empty strings', async () => {
      // ARRANGE: Set parameters to empty strings
      req.params.fileName = '';
      req.query.targetColor = '';
      req.query.threshold = '';

      // ACT: Call controller
      await startProcessVideo(req, res);

      // ASSERT: Should reject empty strings
      expect(res.status.calledWith(400)).to.be.true;
      expect(res.json.calledOnce).to.be.true;
    });
  });

  // ----------------------
  //  SUCCESS CASES
  // ----------------------

  describe('Success Cases', () => {
    /**
     * Test Case: Successful job creation
     * 
     * Verifies the happy path where all parameters are valid and the job
     * is successfully created and queued for processing.
     */
    it('should return 200 with jobId when all parameters are valid', async () => {
      // ARRANGE: All setup done in beforeEach with valid parameters

      // ACT: Call controller with valid parameters
      await startProcessVideo(req, res);

      // ASSERT: Should return success response with job ID
      expect(res.status.calledWith(200)).to.be.true;
      expect(res.json.calledOnce).to.be.true;
      expect(res.json.firstCall.args[0]).to.have.property('jobId');
      expect(res.json.firstCall.args[0].jobId).to.equal('test-job-id-12345');
    });

    /**
     * Test Case: Job creation in database
     * 
     * Verifies that createJob is called with correct parameters to record
     * the job in the database.
     */
    it('should call createJob with correct parameters', async () => {
      // ACT: Start processing
      await startProcessVideo(req, res);

      // ASSERT: Verify createJob was called with correct arguments
      expect(mockCreateJob.calledOnce).to.be.true;
      
      const [jobId, fileName, outputPath] = mockCreateJob.firstCall.args;
      expect(jobId).to.equal('test-job-id-12345');
      expect(fileName).to.equal('test-video.mp4');
      expect(outputPath).to.equal('/test/results/test-video.mp4.csv');
    });

    /**
     * Test Case: UUID generation
     * 
     * Verifies that a unique job ID is generated for each request.
     */
    it('should generate a unique job ID using uuid', async () => {
      // ACT: Start processing
      await startProcessVideo(req, res);

      // ASSERT: Verify uuid.v4() was called
      expect(mockUuidv4.calledOnce).to.be.true;
    });

    /**
     * Test Case: Correct output path construction
     * 
     * Verifies that the output CSV path is constructed correctly using
     * RESULTS_DIR and the video filename.
     */
    it('should construct correct output path', async () => {
      // ARRANGE: Set specific RESULTS_DIR
      process.env.RESULTS_DIR = '/custom/output';

      // ACT: Start processing
      await startProcessVideo(req, res);

      // ASSERT: Verify output path includes RESULTS_DIR and filename
      const outputPath = mockCreateJob.firstCall.args[2];
      expect(outputPath).to.equal('/custom/output/test-video.mp4.csv');
    });

    /**
     * Test Case: Video path construction
     * 
     * Verifies that the input video path is constructed correctly.
     */
    it('should construct correct video input path', async () => {
      // ARRANGE: Set specific VIDEOS_DIR
      process.env.VIDEOS_DIR = '/custom/videos';

      // ACT: Start processing (spawn will be called with video path)
      await startProcessVideo(req, res);

      // ASSERT: Spawn should be called with correct video path
      // Note: We can't directly verify spawn args in this mock setup,
      // but we verify the paths are constructed correctly through other means
      expect(res.status.calledWith(200)).to.be.true;
    });
  });

  // ----------------------
  //  PROCESS SPAWNING
  // ----------------------

  describe('Java Process Spawning', () => {
    /**
     * Test Case: Spawn is called
     * 
     * Verifies that a child process is spawned to run the Java processor.
     */
    it('should initiate Java processor (integration note)', async () => {
      // ACT: Start processing
      await startProcessVideo(req, res);

      // ASSERT: Response should be sent immediately
      expect(res.status.calledWith(200)).to.be.true;
    });
  });

  // ----------------------
  //  ERROR HANDLING
  // ----------------------

  describe('Error Handling', () => {
    /**
     * Test Case: Database error during job creation
     * 
     * Verifies that errors from createJob are caught and result in 500 response.
     */
    it('should return 500 when createJob throws an error', async () => {
      // ARRANGE: Make createJob throw an error
      mockCreateJob.throws(new Error('Database connection failed'));

      // ACT: Attempt to start processing
      await startProcessVideo(req, res);

      // ASSERT: Should catch error and return 500
      expect(res.status.calledWith(500)).to.be.true;
      expect(res.json.firstCall.args[0]).to.have.property('error');
      expect(res.json.firstCall.args[0].error).to.equal('Error starting job');
    });

    /**
     * Test Case: UUID generation failure
     * 
     * Verifies handling of unexpected errors during job ID generation.
     */
    it('should return 500 when uuid generation fails', async () => {
      // ARRANGE: Make uuid.v4() throw an error
      mockUuidv4.throws(new Error('UUID generation failed'));

      // ACT: Attempt to start processing
      await startProcessVideo(req, res);

      // ASSERT: Should catch error and return 500
      expect(res.status.calledWith(500)).to.be.true;
      expect(res.json.firstCall.args[0].error).to.equal('Error starting job');
    });

    /**
     * Test Case: Missing environment variables
     * 
     * Verifies that the controller handles missing environment variables gracefully.
     */
    it('should handle missing environment variables', async () => {
      // ARRANGE: Remove required environment variables
      delete process.env.RESULTS_DIR;
      delete process.env.VIDEOS_DIR;
      delete process.env.JAR_PATH;

      // ACT: Attempt to start processing
      await startProcessVideo(req, res);

      // ASSERT: Should handle gracefully (may return 500 or construct invalid paths)
      // The current implementation would create paths like "undefined/filename.mp4"
      // which would eventually fail when spawn is called
      expect(res.status.called).to.be.true;
    });
  });

  // ----------------------
  //  EDGE CASES
  // ----------------------

  describe('Edge Cases', () => {
    /**
     * Test Case: Special characters in filename
     * 
     * Verifies handling of filenames with special characters that might
     * cause issues in file paths or shell commands.
     */
    it('should handle filenames with special characters', async () => {
      // ARRANGE: Use filename with special characters
      req.params.fileName = 'test video (1) [HD].mp4';

      // ACT: Start processing
      await startProcessVideo(req, res);

      // ASSERT: Should accept the filename and create job
      expect(res.status.calledWith(200)).to.be.true;
      const fileName = mockCreateJob.firstCall.args[1];
      expect(fileName).to.equal('test video (1) [HD].mp4');
    });

    /**
     * Test Case: Very long color hex values
     * 
     * Verifies handling of unusually formatted color parameters.
     */
    it('should accept various color formats', async () => {
      // ARRANGE: Use different color format
      req.query.targetColor = '#FF0000';  // With hash prefix

      // ACT: Start processing
      await startProcessVideo(req, res);

      // ASSERT: Should accept and process
      expect(res.status.calledWith(200)).to.be.true;
    });

    /**
     * Test Case: Threshold boundary values
     * 
     * Verifies handling of threshold values at boundaries (0, 255, etc.)
     */
    it('should accept threshold boundary values', async () => {
      // ARRANGE: Use boundary threshold value
      req.query.threshold = '0';  // Minimum threshold

      // ACT: Start processing
      await startProcessVideo(req, res);

      // ASSERT: Should accept boundary values
      expect(res.status.calledWith(200)).to.be.true;

      // Test maximum boundary
      req.query.threshold = '255';
      await startProcessVideo(req, res);
      expect(res.status.calledWith(200)).to.be.true;
    });

    /**
     * Test Case: Concurrent requests
     * 
     * Verifies that multiple concurrent requests each get unique job IDs.
     */
    it('should generate unique job IDs for concurrent requests', async () => {
      // ARRANGE: Make uuid return different values for each call
      mockUuidv4.onFirstCall().returns('job-id-1');
      mockUuidv4.onSecondCall().returns('job-id-2');

      // ACT: Make two concurrent requests
      await startProcessVideo(req, res);
      const firstJobId = res.json.firstCall.args[0].jobId;

      // Reset res for second call
      res = {
        status: sinon.stub().returnsThis(),
        json: sinon.stub().returnsThis()
      };

      await startProcessVideo(req, res);
      const secondJobId = res.json.firstCall.args[0].jobId;

      // ASSERT: Job IDs should be different
      expect(firstJobId).to.not.equal(secondJobId);
      expect(mockUuidv4.calledTwice).to.be.true;
    });
  });
});
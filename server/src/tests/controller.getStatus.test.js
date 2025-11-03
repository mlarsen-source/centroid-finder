// Import testing libraries
import { expect } from 'chai';  // Assertion library for readable test expectations
import sinon from 'sinon';      // Mocking/stubbing library for test doubles
import esmock from 'esmock';    // ES module mocking tool to replace imports

describe('getStatus Controller', () => {
  // Declare variables that will be used across all tests
  let req, res, getStatus, mockCheckJob;

  beforeEach(async () => {
    // Setup mock request object - simulates Express request
    req = {
      params: {
        jobId: 'test-job-id-123'  // Mock job ID from URL parameter
      }
    };

    // Setup mock response object - simulates Express response
    // Each method is stubbed and returns 'this' to allow method chaining (e.g., res.status().json())
    res = {
      status: sinon.stub().returnsThis(),  // Stub for setting HTTP status code
      json: sinon.stub().returnsThis()     // Stub for sending JSON response
    };

    // Create a stub for the checkJob function that we'll control in each test
    mockCheckJob = sinon.stub();

    // Use esmock.strict() to load the controller with mocked dependencies
    // This replaces the real repos module with our mock when the controller imports it
    const controllerModule = await esmock.strict('../controllers/controller.js', {
      // The key must match how the controller imports repos (from controller's perspective)
      '../controllers/../repos/repos.js': {
        checkJob: mockCheckJob,           // Our controllable mock function
        createJob: sinon.stub(),          // Stub other functions to prevent errors
        updateStatus: sinon.stub()        // Even though getStatus doesn't use these
      }
    });

    // Extract the getStatus function from the mocked module
    getStatus = controllerModule.getStatus;
  });

  afterEach(() => {
    // Restore all Sinon stubs to their original state after each test
    // This prevents test pollution and ensures clean state between tests
    sinon.restore();
  });

  describe('Success Cases', () => {
    it('should return 200 with processing status when job is processing', async () => {
      // ARRANGE: Set up the mock to return a processing status
      mockCheckJob.resolves({
        status: 'processing',
        outputPath: null
      });

      // ACT: Call the controller function with our mock req/res objects
      await getStatus(req, res);

      // ASSERT: Verify the function behaved correctly
      expect(mockCheckJob.calledOnceWith('test-job-id-123')).to.be.true;  // Called with correct jobId
      expect(res.status.calledWith(200)).to.be.true;                       // HTTP 200 status
      expect(res.json.calledWith({ status: 'processing' })).to.be.true;   // Correct JSON response
    });

    it('should return 200 with done status and outputPath when job is complete', async () => {
      // ARRANGE: Mock a completed job with an output file path
      const mockOutputPath = '/results/video.csv';
      mockCheckJob.resolves({
        status: 'done',
        outputPath: mockOutputPath
      });

      // ACT: Execute the controller function
      await getStatus(req, res);

      // ASSERT: Verify correct behavior for completed jobs
      expect(mockCheckJob.calledOnceWith('test-job-id-123')).to.be.true;
      expect(res.status.calledWith(200)).to.be.true;
      expect(res.json.calledWith({
        status: 'done',
        outputPath: mockOutputPath  // Should include the output file path
      })).to.be.true;
    });

    it('should return 200 with error status and message when job failed', async () => {
      // ARRANGE: Mock a failed job
      mockCheckJob.resolves({
        status: 'error',
        outputPath: null
      });

      // ACT: Call the controller
      await getStatus(req, res);

      // ASSERT: Verify error response includes error message
      expect(mockCheckJob.calledOnceWith('test-job-id-123')).to.be.true;
      expect(res.status.calledWith(200)).to.be.true;  // Note: Still 200, not 500
      expect(res.json.calledWith({
        status: 'error',
        error: 'Error processing video: Unexpected ffmpeg error'
      })).to.be.true;
    });
  });

  describe('Error Cases', () => {
    it('should return 404 when job ID is not found (invalid status)', async () => {
      // ARRANGE: Mock returns a status that's not in the allowed list
      mockCheckJob.resolves({
        status: 'unknown',  // This status is not in ['error', 'processing', 'done']
        outputPath: null
      });

      // ACT: Call the controller
      await getStatus(req, res);

      // ASSERT: Should return 404 for unknown status
      expect(res.status.calledWith(404)).to.be.true;
      expect(res.json.calledWith({ error: 'Job ID not found' })).to.be.true;
    });

    it('should return 404 when status is null', async () => {
      // ARRANGE: Mock returns null status (job doesn't exist)
      mockCheckJob.resolves({
        status: null,
        outputPath: null
      });

      // ACT: Execute controller
      await getStatus(req, res);

      // ASSERT: Null status should also return 404
      expect(res.status.calledWith(404)).to.be.true;
      expect(res.json.calledWith({ error: 'Job ID not found' })).to.be.true;
    });

    it('should return 500 when checkJob throws an error', async () => {
      // ARRANGE: Mock throws an error (simulates database failure)
      mockCheckJob.rejects(new Error('Database error'));

      // ACT: Call controller (should catch the error)
      await getStatus(req, res);

      // ASSERT: Should return 500 for unexpected errors
      expect(res.status.calledWith(500)).to.be.true;
      expect(res.json.calledWith({ error: 'Error fetching job status' })).to.be.true;
    });

    it('should return 500 when checkJob returns undefined', async () => {
      // ARRANGE: Mock returns undefined (unexpected response)
      mockCheckJob.resolves(undefined);

      // ACT: Execute the controller
      await getStatus(req, res);

      // ASSERT: Undefined should be caught and return 500
      expect(res.status.calledWith(500)).to.be.true;
      expect(res.json.calledWith({ error: 'Error fetching job status' })).to.be.true;
    });
  });

  describe('Edge Cases', () => {
    it('should handle missing jobId parameter', async () => {
      // ARRANGE: Simulate missing jobId in request params
      req.params.jobId = undefined;
      mockCheckJob.rejects(new Error('No jobId provided'));

      // ACT: Call controller with undefined jobId
      await getStatus(req, res);

      // ASSERT: Should catch error and return 500
      expect(res.status.calledWith(500)).to.be.true;
      expect(res.json.calledWith({ error: 'Error fetching job status' })).to.be.true;
    });

    it('should handle empty string jobId', async () => {
      // ARRANGE: Simulate empty string as jobId
      req.params.jobId = '';
      mockCheckJob.resolves({
        status: 'error',
        outputPath: null
      });

      // ACT: Call controller with empty jobId
      await getStatus(req, res);

      // ASSERT: Should still call checkJob with the empty string
      expect(mockCheckJob.calledOnceWith('')).to.be.true;
    });

    it('should only accept allowed statuses', async () => {
      // ARRANGE: Mock returns a disallowed status value
      const disallowedStatus = 'pending';  // Not in ['error', 'processing', 'done']
      mockCheckJob.resolves({
        status: disallowedStatus,
        outputPath: null
      });

      // ACT: Execute controller
      await getStatus(req, res);

      // ASSERT: Disallowed status should return 404
      expect(res.status.calledWith(404)).to.be.true;
      expect(res.json.calledWith({ error: 'Job ID not found' })).to.be.true;
    });
  });
});

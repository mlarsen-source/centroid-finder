// Import testing libraries
import { expect } from 'chai';          // Assertion library for test expectations
import sinon from 'sinon';              // Mocking/stubbing library for test doubles
import esmock from 'esmock';            // ES module mocking tool to replace imports
import { EventEmitter } from 'events';  // For creating mock child process

/**
 * Unit Test Suite for runProcessor Function
 * 
 * Purpose: Verify that runProcessor correctly:
 * - Spawns Java child processes with correct arguments
 * - Configures process options properly (detached, stdio)
 * - Handles process stdout/stderr streams
 * - Calls updateStatus on process success (exit code 0)
 * - Calls updateStatus on process failure (non-zero exit code)
 * - Handles process startup errors (JAR not found, etc.)
 * - Properly detaches the process and unrefs it
 */

/**
 * Mock Child Process Factory
 * 
 * Creates a fake child process that behaves like Node's child_process.spawn() result.
 * Uses EventEmitter to simulate the event-driven nature of child processes.
 * 
 * @returns {Object} Mock child process with stdout, stderr, and event emission
 */
function createMockChildProcess() {
  const mockProcess = new EventEmitter();
  
  // Child processes have stdout and stderr streams (also EventEmitters)
  mockProcess.stdout = new EventEmitter();
  mockProcess.stderr = new EventEmitter();
  
  // unref() is called to allow the parent process to exit independently
  mockProcess.unref = sinon.stub();
  
  return mockProcess;
}

describe('runProcessor Function', () => {
  let mockSpawn, mockUpdateStatus, mockChildProcess;
  let runProcessor;  // Will hold the function under test

  /**
   * Setup: Runs before each test
   * 
   * Creates fresh mocks and loads the runProcessor function with mocked dependencies.
   * This ensures each test starts with a clean slate.
   */
  beforeEach(async () => {
    // Create a mock child process that we control
    mockChildProcess = createMockChildProcess();
    
    // Stub spawn to return our mock child process instead of creating a real one
    mockSpawn = sinon.stub().returns(mockChildProcess);
    
    // Stub updateStatus to track when it's called
    mockUpdateStatus = sinon.stub();

    // Use esmock to load the controller with mocked dependencies
    const controllerModule = await esmock.strict('../controllers/controller.js', {
      '../controllers/../repos/repos.js': {
        createJob: sinon.stub(),
        checkJob: sinon.stub(),
        updateStatus: mockUpdateStatus  // Our controllable mock
      },
      'child_process': {
        spawn: mockSpawn  // Return our mock child process
      }
    });

    // Extract the runProcessor function from the mocked module
    runProcessor = controllerModule.runProcessor;
  });

  /**
   * Teardown: Runs after each test
   * 
   * Restores all Sinon stubs to prevent test pollution.
   */
  afterEach(() => {
    sinon.restore();
  });

  // ----------------------
  //  PROCESS SPAWNING
  // ----------------------

  describe('Process Spawning', () => {
    /**
     * Test Case: Spawn called with correct command
     * 
     * Verifies that spawn is called with 'java' as the command.
     */
    it('should spawn a java process', () => {
      // ARRANGE: Set up test parameters
      const jarPath = '/path/to/processor.jar';
      const videoPath = '/videos/test.mp4';
      const outputPath = '/results/test.csv';
      const targetColor = 'FF0000';
      const threshold = '50';
      const jobId = 'test-job-123';

      // ACT: Call runProcessor
      runProcessor(jarPath, videoPath, outputPath, targetColor, threshold, jobId);

      // ASSERT: Verify spawn was called with 'java'
      expect(mockSpawn.calledOnce).to.be.true;
      expect(mockSpawn.firstCall.args[0]).to.equal('java');
    });

    /**
     * Test Case: Correct arguments passed to Java process
     * 
     * Verifies that all arguments are passed to the JAR in the correct order.
     */
    it('should pass correct arguments to the JAR', () => {
      // ARRANGE
      const jarPath = '/path/to/processor.jar';
      const videoPath = '/videos/test.mp4';
      const outputPath = '/results/test.csv';
      const targetColor = 'FF0000';
      const threshold = '50';
      const jobId = 'test-job-123';

      // ACT
      runProcessor(jarPath, videoPath, outputPath, targetColor, threshold, jobId);

      // ASSERT: Verify arguments array
      const spawnArgs = mockSpawn.firstCall.args[1];
      expect(spawnArgs).to.deep.equal([
        '-jar',
        jarPath,
        videoPath,
        outputPath,
        targetColor,
        threshold
      ]);
    });

    /**
     * Test Case: Process options configured correctly
     * 
     * Verifies that spawn is called with correct options for detached process.
     */
    it('should configure process options correctly', () => {
      // ARRANGE
      const jarPath = '/path/to/processor.jar';
      const videoPath = '/videos/test.mp4';
      const outputPath = '/results/test.csv';
      const targetColor = 'FF0000';
      const threshold = '50';
      const jobId = 'test-job-123';

      // ACT
      runProcessor(jarPath, videoPath, outputPath, targetColor, threshold, jobId);

      // ASSERT: Verify spawn options
      const spawnOptions = mockSpawn.firstCall.args[2];
      expect(spawnOptions.shell).to.be.false;          // No shell injection risk
      expect(spawnOptions.detached).to.be.true;        // Process runs independently
      expect(spawnOptions.stdio).to.deep.equal(['ignore', 'pipe', 'pipe']);  // stdin ignored, stdout/stderr piped
    });

    /**
     * Test Case: Process unref called
     * 
     * Verifies that unref() is called to allow the parent process to exit
     * without waiting for the child.
     */
    it('should call unref on the child process', () => {
      // ARRANGE
      const jarPath = '/path/to/processor.jar';
      const videoPath = '/videos/test.mp4';
      const outputPath = '/results/test.csv';
      const targetColor = 'FF0000';
      const threshold = '50';
      const jobId = 'test-job-123';

      // ACT
      runProcessor(jarPath, videoPath, outputPath, targetColor, threshold, jobId);

      // ASSERT: Verify unref was called
      expect(mockChildProcess.unref.calledOnce).to.be.true;
    });
  });

  // ----------------------
  //  SUCCESS CASES
  // ----------------------

  describe('Success Cases', () => {
    /**
     * Test Case: Successful process completion (exit code 0)
     * 
     * Verifies that when the Java process exits with code 0 (success),
     * updateStatus is called with true to mark the job as done.
     */
    it('should call updateStatus with true on successful completion', (done) => {
      // ARRANGE
      const jobId = 'test-job-123';
      
      // ACT: Start the processor
      runProcessor(
        '/path/to/processor.jar',
        '/videos/test.mp4',
        '/results/test.csv',
        'FF0000',
        '50',
        jobId
      );

      // Simulate successful process completion
      mockChildProcess.emit('close', 0);  // Exit code 0 = success

      // ASSERT: Use setTimeout to allow async event handling
      setTimeout(() => {
        expect(mockUpdateStatus.calledOnce).to.be.true;
        expect(mockUpdateStatus.calledWith(jobId, true)).to.be.true;
        done();
      }, 10);
    });

    /**
     * Test Case: Process stdout data captured
     * 
     * Verifies that stdout data from the Java process is logged.
     * Uses sinon.spy on console.log to verify logging behavior.
     */
    it('should log stdout data from the Java process', (done) => {
      // ARRANGE: Spy on console.log
      const consoleLogSpy = sinon.spy(console, 'log');
      
      // ACT: Start the processor
      runProcessor(
        '/path/to/processor.jar',
        '/videos/test.mp4',
        '/results/test.csv',
        'FF0000',
        '50',
        'test-job-123'
      );

      // Simulate stdout data
      const testData = Buffer.from('Processing frame 100...');
      mockChildProcess.stdout.emit('data', testData);

      // ASSERT
      setTimeout(() => {
        expect(consoleLogSpy.called).to.be.true;
        expect(consoleLogSpy.calledWith('Processing frame 100...')).to.be.true;
        consoleLogSpy.restore();
        done();
      }, 10);
    });
  });

  // ----------------------
  //  ERROR CASES
  // ----------------------

  describe('Error Cases', () => {
    /**
     * Test Case: Process exits with non-zero code
     * 
     * Verifies that when the Java process exits with a non-zero code (failure),
     * updateStatus is called with false to mark the job as error.
     */
    it('should call updateStatus with false on process failure', (done) => {
      // ARRANGE
      const jobId = 'test-job-123';
      
      // ACT: Start the processor
      runProcessor(
        '/path/to/processor.jar',
        '/videos/test.mp4',
        '/results/test.csv',
        'FF0000',
        '50',
        jobId
      );

      // Simulate process failure with exit code 1
      mockChildProcess.emit('close', 1);  // Non-zero = failure

      // ASSERT
      setTimeout(() => {
        expect(mockUpdateStatus.calledOnce).to.be.true;
        expect(mockUpdateStatus.calledWith(jobId, false)).to.be.true;
        done();
      }, 10);
    });

    /**
     * Test Case: JAR file not found error
     * 
     * Verifies that when spawn fails (e.g., JAR doesn't exist),
     * the error event is handled and updateStatus is called with false.
     */
    it('should handle JAR not found errors', (done) => {
      // ARRANGE: Spy on console.error
      const consoleErrorSpy = sinon.spy(console, 'error');
      const jobId = 'test-job-123';
      
      // ACT: Start the processor
      runProcessor(
        '/path/to/nonexistent.jar',
        '/videos/test.mp4',
        '/results/test.csv',
        'FF0000',
        '50',
        jobId
      );

      // Simulate spawn error (JAR not found)
      const error = new Error('ENOENT: no such file or directory');
      mockChildProcess.emit('error', error);

      // ASSERT
      setTimeout(() => {
        expect(consoleErrorSpy.called).to.be.true;
        expect(mockUpdateStatus.calledOnce).to.be.true;
        expect(mockUpdateStatus.calledWith(jobId, false)).to.be.true;
        consoleErrorSpy.restore();
        done();
      }, 10);
    });

    /**
     * Test Case: Process crashes (exit code 137 - killed)
     * 
     * Verifies handling of processes that are killed externally.
     */
    it('should handle killed processes correctly', (done) => {
      // ARRANGE
      const jobId = 'test-job-123';
      
      // ACT
      runProcessor(
        '/path/to/processor.jar',
        '/videos/test.mp4',
        '/results/test.csv',
        'FF0000',
        '50',
        jobId
      );

      // Simulate process killed (exit code 137 = SIGKILL)
      mockChildProcess.emit('close', 137);

      // ASSERT: Should mark as failure
      setTimeout(() => {
        expect(mockUpdateStatus.calledWith(jobId, false)).to.be.true;
        done();
      }, 10);
    });
  });

  // ----------------------
  //  EDGE CASES
  // ----------------------

  describe('Edge Cases', () => {
    /**
     * Test Case: Multiple rapid stdout emissions
     * 
     * Verifies that multiple stdout data events are handled correctly.
     */
    it('should handle multiple stdout data events', (done) => {
      // ARRANGE
      const consoleLogSpy = sinon.spy(console, 'log');
      
      // ACT
      runProcessor(
        '/path/to/processor.jar',
        '/videos/test.mp4',
        '/results/test.csv',
        'FF0000',
        '50',
        'test-job-123'
      );

      // Simulate multiple stdout emissions
      mockChildProcess.stdout.emit('data', Buffer.from('Line 1'));
      mockChildProcess.stdout.emit('data', Buffer.from('Line 2'));
      mockChildProcess.stdout.emit('data', Buffer.from('Line 3'));

      // ASSERT
      setTimeout(() => {
        // Verify that at least 3 logs occurred (might be more due to "child spawned")
        expect(consoleLogSpy.callCount).to.be.at.least(3);
        
        // Verify the specific stdout lines were logged
        const loggedMessages = consoleLogSpy.getCalls().map(call => call.args[0]);
        expect(loggedMessages).to.include('Line 1');
        expect(loggedMessages).to.include('Line 2');
        expect(loggedMessages).to.include('Line 3');
        
        consoleLogSpy.restore();
        done();
      }, 10);
    });

    /**
     * Test Case: Process completes before error event
     * 
     * Verifies that if close event fires before error is emitted,
     * updateStatus is still called correctly.
     */
    it('should only call updateStatus once on close', (done) => {
      // ARRANGE
      const jobId = 'test-job-123';
      
      // ACT
      runProcessor(
        '/path/to/processor.jar',
        '/videos/test.mp4',
        '/results/test.csv',
        'FF0000',
        '50',
        jobId
      );

      // Emit close event
      mockChildProcess.emit('close', 0);

      // ASSERT
      setTimeout(() => {
        expect(mockUpdateStatus.calledOnce).to.be.true;
        done();
      }, 10);
    });

    /**
     * Test Case: Special characters in paths
     * 
     * Verifies that paths with special characters are passed correctly.
     */
    it('should handle special characters in paths', () => {
      // ARRANGE: Paths with spaces and special characters
      const jarPath = '/path/to/my processor (v2).jar';
      const videoPath = '/videos/test [HD] (1).mp4';
      const outputPath = '/results/test output.csv';

      // ACT
      runProcessor(
        jarPath,
        videoPath,
        outputPath,
        'FF0000',
        '50',
        'test-job-123'
      );

      // ASSERT: Arguments should be passed as-is (not escaped, since shell: false)
      const spawnArgs = mockSpawn.firstCall.args[1];
      expect(spawnArgs[1]).to.equal(jarPath);
      expect(spawnArgs[2]).to.equal(videoPath);
      expect(spawnArgs[3]).to.equal(outputPath);
    });
  });
});
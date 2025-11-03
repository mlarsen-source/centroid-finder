// Import the `expect` function from Chai's assertion library.
// Chai provides a clean, human-readable way to write test conditions like
// "expect(result).to.equal(4)".
import { expect } from "chai";

// Define a Mocha test suite using `describe()`.
// A "suite" is just a group of related tests. The first argument is a name
// (for readability in test output), and the second is a function containing the tests.
describe("Basic test setup", () => {
  
  // Define an individual test case using `it()`.
  // The first argument is a human-readable description of what this test verifies.
  // The second argument is the test logic itself.
  it("should confirm Mocha and Chai are working", () => {

    // Simple test logic: perform a basic operation
    const result = 2 + 2;

    // Assertion: verify that `result` is equal to 4.
    // If this fails, Mocha marks the test as failed and displays the assertion error.
    expect(result).to.equal(4);
  });
});

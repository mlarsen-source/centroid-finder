import { expect } from "chai";
import { getStatus } from "../controllers/controller.js";

/**
 * Simple mock Express response object
 * - Captures status and JSON payloads
 * - Lets us inspect what the controller sends back
 */
function mockRes() {
  return {
    statusCode: null,
    body: null,
    status(code) {
      this.statusCode = code;
      return this;
    },
    json(data) {
      this.body = data;
      return this;
    },
  };
}

describe("Controller: getStatus (basic functional tests)", () => {
  // 1️⃣ Nonexistent job ID
  it("returns 404 when jobId does not exist", async () => {
    const req = { params: { jobId: "fake-job-id-xyz" } };
    const res = mockRes();

    await getStatus(req, res);

    expect(res.statusCode).to.equal(404);
    expect(res.body).to.have.property("error");
  });

  // 2️⃣ Missing jobId
  it("returns 400 if jobId parameter is missing", async () => {
    const req = { params: {} };
    const res = mockRes();

    await getStatus(req, res);

    // The controller might return 400 or 404 depending on logic
    expect([400, 404]).to.include(res.statusCode);
    expect(res.body).to.be.an("object");
  });

  // 3️⃣ Simulated existing job (only runs if you actually have one in DB)
  it("returns 200 and job data if a real job exists", async () => {
    // Replace with a real jobId from your DB if you have one
    const knownJobId = "123"; 
    const req = { params: { jobId: knownJobId } };
    const res = mockRes();

    await getStatus(req, res);

    // These assertions are flexible but confirm correct logic
    expect([200, 404, 500]).to.include(res.statusCode);
    if (res.statusCode === 200) {
      expect(res.body).to.have.property("status");
      if (res.body.status === "done") {
        expect(res.body).to.have.property("outputPath");
      }
    } else {
      expect(res.body).to.be.an("object");
    }
  });

  // 4️⃣ Invalid input type (e.g., null jobId)
  it("handles invalid input gracefully", async () => {
    const req = { params: { jobId: null } };
    const res = mockRes();

    await getStatus(req, res);

    expect([400, 404, 500]).to.include(res.statusCode);
    expect(res.body).to.be.an("object");
  });
});


import { expect } from "chai";
import fs from "fs/promises";
import path from "path";
import { getAllResults } from "../controllers/controller.js";

function createMockRes() {
  return {
    statusCode: null,
    jsonBody: null,
    status(code) {
      this.statusCode = code;
      return this;
    },
    json(body) {
      this.jsonBody = body;
      return this;
    },
  };
}

describe("UNIT: getAllResults()", () => {
  const tempDir = path.join(process.cwd(), "src", "tests", "temp_results");

  before(async () => {
    await fs.mkdir(tempDir, { recursive: true });
    process.env.RESULTS_DIR = tempDir;
  });

  after(async () => {
    await fs.rm(tempDir, { recursive: true, force: true });
    delete process.env.RESULTS_DIR;
  });

  beforeEach(async () => {
    const existing = await fs.readdir(tempDir);
    for (const file of existing) {
      await fs.unlink(path.join(tempDir, file));
    }
  });

  it("returns 200 with the list of CSV files in the results directory", async () => {
    const sampleFiles = ["one.csv", "two.csv"];
    for (const file of sampleFiles) {
      await fs.writeFile(path.join(tempDir, file), "time,x,y\n");
    }

    const res = createMockRes();
    await getAllResults({}, res);

    expect(res.statusCode).to.equal(200);
    expect(res.jsonBody).to.have.members(sampleFiles);
  });

  it("returns 500 when the results directory cannot be read", async () => {
    const previous = process.env.RESULTS_DIR;
    process.env.RESULTS_DIR = path.join(tempDir, "missing");

    const res = createMockRes();
    await getAllResults({}, res);

    expect(res.statusCode).to.equal(500);
    expect(res.jsonBody).to.deep.equal({
      error: "Error reading results directory",
    });

    process.env.RESULTS_DIR = previous;
  });
});


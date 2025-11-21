import { expect } from "chai";
import fsPromises from "fs/promises";
import path from "path";
import { Writable } from "stream";
import { getCsv } from "../controllers/controller.js";

class MockStreamResponse extends Writable {
  constructor() {
    super();
    this.statusCode = null;
    this.headers = {};
    this.body = Buffer.alloc(0);
    this.sentPayload = null;
    this.finished = new Promise((resolve, reject) => {
      this.on("finish", resolve);
      this.on("error", reject);
    });
  }

  status(code) {
    this.statusCode = code;
    return this;
  }

  set(headers) {
    Object.assign(this.headers, headers);
    return this;
  }

  send(payload) {
    this.sentPayload = payload;
    return this;
  }

  _write(chunk, encoding, callback) {
    const buffer = Buffer.isBuffer(chunk)
      ? chunk
      : Buffer.from(chunk, encoding);
    this.body = Buffer.concat([this.body, buffer]);
    callback();
  }
}

describe("UNIT: getCsv()", () => {
  const tempDir = path.join(process.cwd(), "src", "tests", "temp_csv_results");

  before(async () => {
    await fsPromises.mkdir(tempDir, { recursive: true });
    process.env.RESULTS_DIR = tempDir;
  });

  after(async () => {
    await fsPromises.rm(tempDir, { recursive: true, force: true });
    delete process.env.RESULTS_DIR;
  });

  it("streams the CSV file with download headers when the file exists", async () => {
    const fileName = "metrics.csv";
    const csvContent = "time,x,y\n0.0,12,5\n";
    const filePath = path.join(tempDir, fileName);
    await fsPromises.writeFile(filePath, csvContent);

    const req = { params: { fileName } };
    const res = new MockStreamResponse();

    await getCsv(req, res);
    await res.finished;

    expect(res.headers["Content-Type"]).to.equal("text/csv");
    expect(res.headers["Content-Disposition"]).to.equal(
      `attachment; filename="${fileName}"`
    );
    expect(res.body.toString()).to.equal(csvContent);
    expect(res.statusCode).to.be.oneOf([null, undefined]);
    expect(res.sentPayload).to.be.null;

    await fsPromises.unlink(filePath);
  });
});


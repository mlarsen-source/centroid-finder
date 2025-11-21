import ffmpegInstaller from "@ffmpeg-installer/ffmpeg";
import { spawn } from "child_process";
import ffmpeg from "fluent-ffmpeg";
import fs from "fs";
import fsP from "fs/promises";
import path from "path";
import { v4 as uuidv4 } from "uuid";
import { checkJob, createJob, updateStatus } from "./../repos/repos.js";

/**
 * Get all video files from the videos directory
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
export const getAllVideos = async (req, res) => {
  try {
    // Read all files from the videos directory
    const videos = await fsP.readdir(process.env.VIDEOS_DIR);
    res.status(200).json(videos);
  } catch {
    res.status(500).json({ error: "Error reading video directory" });
  }
};

/**
 * Get all result files from the results directory
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
export const getAllResults = async (req, res) => {
  try {
    // Read all files from the results directory
    const results = await fsP.readdir(process.env.RESULTS_DIR);
    res.status(200).json(results);
  } catch {
    res.status(500).json({ error: "Error reading results directory" });
  }
};

/**
 * Generate and return a thumbnail image for a video file
 * @param {Object} req - Express request object with fileName param
 * @param {Object} res - Express response object
 */
export const getThumbnail = async (req, res) => {
  // Set the FFmpeg binary path from the installer
  ffmpeg.setFfmpegPath(ffmpegInstaller.path);
  const { fileName } = req.params;
  // Construct the full path to the video file
  const videoPath = path.join(process.env.VIDEOS_DIR, fileName);
  // Create a temporary path for the thumbnail image
  const tempImage = path.join("./public", `${fileName}-thumb.jpg`);

  try {
    // Wrap FFmpeg operation in a promise
    await new Promise((resolve, reject) => {
      ffmpeg(videoPath)
        // When thumbnail generation is complete
        .on("end", () => {
          // Send the thumbnail file to the client
          res.status(200).sendFile(path.resolve(tempImage), (err) => {
            // Delete the temporary thumbnail file after sending
            fs.unlink(tempImage, () => {});
          });
          resolve();
        })
        // Handle any FFmpeg errors
        .on("error", (err) => {
          console.error("FFmpeg error:", err);
          reject(err);
        })
        // Generate a screenshot at timestamp 0 (first frame)
        .screenshots({
          timestamps: [0],
          filename: path.basename(tempImage),
          folder: "./public",
        });
    });
  } catch (err) {
    res.status(500).json({ error: "Error generating thumbnail" });
  }
};

/**
 * Start a video processing job with the Java processor
 * @param {Object} req - Express request object with fileName param and query params
 * @param {Object} res - Express response object
 */
export const startProcessVideo = async (req, res) => {
  const { fileName } = req.params;
  const { targetColor, threshold } = req.query;
  try {
    // Validate that all required parameters are provided
    if (!fileName || !targetColor || !threshold) {
      res
        .status(400)
        .json({ error: "Missing targetColor or threshold query parameter." });
      return;
    }

    // Generate a unique job ID for tracking
    const jobId = uuidv4();
    // Construct the output path for the CSV result
    const outputPath = `${process.env.RESULTS_DIR}/${fileName}.csv`;
    // Construct the full path to the input video
    const videoPath = `${process.env.VIDEOS_DIR}/${fileName}`;

    console.log("Creating Job");
    // Create a new job entry in the database
    createJob(jobId, fileName, outputPath);

    // Get the absolute path to the Java JAR file
    const jarPath = path.resolve(process.env.JAR_PATH);

    console.log("running processor");
    // Start the Java processor in a separate process
    runProcessor(jarPath, videoPath, outputPath, targetColor, threshold, jobId);

    // Return the job ID to the client for status tracking
    res.status(200).json({ jobId });
  } catch {
    res.status(500).json({ error: "Error starting job" });
  }
};

/**
 * Run the Java video processor as a detached child process
 * @param {string} jarPath - Path to the Java JAR file
 * @param {string} videoPath - Path to the input video
 * @param {string} outputPath - Path for the output CSV
 * @param {string} targetColor - Target color for processing
 * @param {string} threshold - Threshold value for color matching
 * @param {string} jobId - Unique job identifier
 */
export function runProcessor(
  jarPath,
  videoPath,
  outputPath,
  targetColor,
  threshold,
  jobId
) {
  // Construct command line arguments for the Java process
  const args = ["-jar", jarPath, videoPath, outputPath, targetColor, threshold];
  console.log("spawning:", "java", ...args);

  // Spawn the Java process as a detached child
  const child = spawn("java", args, {
    shell: false, // Don't use a shell
    stdio: ["ignore", "pipe", "pipe"], // Ignore stdin, pipe stdout and stderr
    detached: true, // Run independently of parent process
  });

  // Unreference the child so parent can exit independently
  child.unref();

  console.log("child spawned changed");

  // Log when the child process successfully starts
  child.on("spawn", () => console.log("child started pid:", child.pid));

  // Capture and log standard output from the Java process
  if (child.stdout) {
    child.stdout.setEncoding("utf8");
    child.stdout.on("data", (data) =>
      process.stdout.write(`[processor:${jobId}] ${data}`)
    );
    child.stdout.on("end", () =>
      console.log(`[processor:${jobId}] stdout ended`)
    );
  }

  // Capture and log standard error from the Java process
  if (child.stderr) {
    child.stderr.setEncoding("utf8");
    child.stderr.on("data", (data) =>
      process.stderr.write(`[processor:${jobId}][err] ${data}`)
    );
    child.stderr.on("end", () =>
      console.log(`[processor:${jobId}] stderr ended`)
    );
  }

  // Handle startup failures (e.g., JAR file not found)
  child.on("error", (err) => {
    console.error("Failed to start JAR process:", err);
    // Update job status to failed in the database
    updateStatus(jobId, false);
  });

  // Handle process exit (both successful and failed)
  child.on("close", (code) => {
    console.log(`[processor:${jobId}] exited with code ${code}`);
    // Update job status based on exit code (0 = success, non-zero = error)
    updateStatus(jobId, code === 0);
  });
}

/**
 * Get the status of a processing job
 * @param {Object} req - Express request object with jobId param
 * @param {Object} res - Express response object
 */
export const getStatus = async (req, res) => {
  try {
    const { jobId } = req.params;

    // Query the database for job status and output path
    const { status, outputPath } = await checkJob(jobId);
    // Define allowed status values
    const allowed = ["error", "processing", "done"];

    // Return 404 if job ID doesn't exist or has invalid status
    if (!allowed.includes(status))
      return res.status(404).json({ error: "Job ID not found" });

    // Return processing status if job is still running
    if (status === "processing")
      return res.status(200).json({ status: "processing" });

    // Return success status with output path if job is complete
    if (status === "done") return res.status(200).json({ status, outputPath });

    // Return error status if job failed
    if (status === "error")
      return res.status(200).json({
        status,
        error: "Error processing video: Unexpected ffmpeg error",
      });
  } catch {
    res.status(500).json({ error: "Error fetching job status" });
  }
};

/**
 * Stream a CSV result file to the client
 * @param {Object} req - Express request object with fileName param
 * @param {Object} res - Express response object
 */
export const getCsv = async (req, res) => {
  const { fileName } = req.params;
  // Construct the full path to the CSV file
  const csvPath = path.join(process.env.RESULTS_DIR, fileName);

  // Check if the file exists before attempting to stream it
  if (!fs.existsSync(csvPath)) {
    console.error(`File not found at: ${csvPath}`);
    return res.status(404).send("CSV file not found.");
  }

  // Set HTTP headers to trigger file download in the browser
  res.set({
    "Content-Type": "text/csv",
    "Content-Disposition": `attachment; filename="${fileName}"`,
  });

  // Create a read stream and pipe it directly to the response
  fs.createReadStream(csvPath)
    .pipe(res) // Stream the file to the client
    .on("error", (err) => {
      console.error("Stream error:", err);
      // Handle streaming errors
      res.status(500).send("Error streaming the file.");
    });
};

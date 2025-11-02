import { createJob, checkJob, updateStatus } from "./../repos/repos.js";
// import { processVideo } from "?";
import ffmpeg from "fluent-ffmpeg";
import ffmpegInstaller from "@ffmpeg-installer/ffmpeg";
import path from "path";
import fs from "fs";
import fsP from "fs/promises";
import { v4 as uuidv4 } from "uuid";
import { spawn } from "child_process";

export const getAllVideos = async (req, res) => {
  try {
    const videos = await fsP.readdir(process.env.VIDEOS_DIR);
    res.status(200).json(videos);
  } catch {
    res.status(500).json({ error: "Error reading video directory" });
  }
};

export const getThumbnail = (req, res) => {
  ffmpeg.setFfmpegPath(ffmpegInstaller.path);
  const { fileName } = req.params;
  const videoPath = path.join(process.env.VIDEOS_DIR, fileName);
  const tempImage = path.join("./public", `${fileName}-thumb.jpg`);

  ffmpeg(videoPath)
    .on("end", () => {
      res.status(200).sendFile(path.resolve(tempImage), (err) => {
        fs.unlink(tempImage, () => {});
      });
    })
    .on("error", (err) => {
      console.error("FFmpeg error:", err);
      res.status(500).json({ error: "Error generating thumbnail" });
    })
    .screenshots({
      timestamps: [0],
      filename: path.basename(tempImage),
      folder: "./public",
    });
};

export const startProcessVideo = async (req, res) => {
  const { fileName } = req.params;
  const { targetColor, threshold } = req.query;
  try {
    if (!fileName || !targetColor || !threshold)
      res
        .status(400)
        .json({ error: "Missing targetColor or threshold query parameter." });

    const jobId = uuidv4();
    const outputPath = `${process.env.RESULTS_DIR}/${fileName}.csv`;
    const videoPath = `${process.env.VIDEOS_DIR}/${fileName}`;

    console.log('Creating Job');
    createJob(jobId, fileName, outputPath);

    const jarPath = path.resolve(
      process.cwd(),
      "../processor/target/centroid-finder-1.0.0-jar-with-dependencies.jar"
    );
    
    console.log('running processor');
    runProcessor(jarPath, videoPath, outputPath, targetColor, threshold, jobId);

    res.status(200).json({ jobId });
  } catch {
    res.status(500).json({ error: "Error starting job" });
  }
};

function runProcessor(jarPath, videoPath, outputPath, targetColor, threshold, jobId) {
  const child = spawn(
    "java",
    ["-jar", jarPath, videoPath, outputPath, targetColor, threshold],
    { shell: false, stdio: ["ignore", "pipe", "pipe"],  detached: true});

  child.unref(); 

  console.log('child spawned');
  child.stdout.on('data', d => console.log(d.toString()));
  child.on("close", code => updateStatus(jobId, code === 0));
}


export const getStatus = async (req, res) => {
  try {
    const { jobId } = req.params;

    const { status, outputPath } = await checkJob(jobId);
    const allowed = ["error", "processing", "done"];

    if (!allowed.includes(status))
      res.status(404).json({ error: "Job ID not found" });

    if (status === "processing") res.status(200).json({ status: "processing" });

    if (status === "done") res.status(200).json({ status, outputPath });

    if (status === "error")
      res
        .status(200)
        .json({
          status,
          error: "Error processing video: Unexpected ffmpeg error",
        });
  } catch {
    res.status(500).json({ error: "Error fetching job status" });
  }
};

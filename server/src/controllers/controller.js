import {createJob, checkJob } from "./../repos/repos.js";
// import { processVideo } from "?";
import ffmpeg from "fluent-ffmpeg";
import path from "path";
import fs from "fs"


export const getAllVideos = async (req, res) => {
  try {
    const videos = await allVideos();

    res.status(200).json({ videos });
  } catch {
    res.status(500).json({ error: "Error reading video directory" });
  }
};

export const getThumbnail = (req, res) => {
  const { fileName } = req.params;
  const videoPath = path.join(process.env.VIDEO_DIR, fileName);
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
    if (!fileName || !targetColor || threshold)
      res.status(400).json({ error: "Missing targetColor or threshold query parameter." });

    //TODO: pick UUID package
    // const jobId = getUUID();
    let jobId;

    //create job in database with status: processing
    createJob(jobId, fileName);

    //start actual job with java
    processVideo(jobId, fileName, targetColor, threshold);

    res.status(200).json({ jobId });
  } catch {
    res.status(500).json({ error: "Error starting job" });
  }
};

export const getStatus = async (req, res) => {
  try {
    const jobId = req.params;
    const { status, result } = checkJob(jobId)

    if (!status) res.status(404).json({ "error": "Job ID not found" });

    if (status === 'processing') res.status(200).json({ "status": "processing" });

    if (status === 'done') res.status(200).json({ status, result });

    res.status(200).json({ status, "error" : "Error processing video: Unexpected ffmpeg error"})

  } catch {
    res.status(500).json({ "error": "Error fetching job status" })
  }
};

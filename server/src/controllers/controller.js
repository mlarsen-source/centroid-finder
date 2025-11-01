import { allVideos, createJob, checkJob } from "./../repos/repos.js";
import { generateThumbnail, processVideo } from "?";

export const getAllVideos = async (req, res) => {
  try {
    const videos = await allVideos();

    res.status(200).json({ videos });
  } catch {
    res.status(500).json({ error: "Error reading video directory" });
  }
};

export const getThumbnail = async (req, res) => {
  try {
    const { fileName } = req.params;
    const thumbnail = await generateThumbnail(fileName);

    res.status(200).sendFile(thumbnail);
  } catch {
    res.status(500).json({ error: "Error generating thumbnail" });
  }
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
    createJob(jobId);

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

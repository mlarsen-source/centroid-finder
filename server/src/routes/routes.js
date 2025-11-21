import { Router } from "express";
import {
  getAllResults,
  getAllVideos,
  getCsv,
  getStatus,
  getThumbnail,
  startProcessVideo,
} from "./../controllers/controller.js";

// Initialize Express router
const router = Router();

//routes

// GET /api/videos - Retrieve list of all available video files
router.get("/api/videos", getAllVideos);

// GET /api/results - Retrieve list of all processed result files
router.get("/api/results", getAllResults);

// GET /api/csv/:fileName - Download a specific CSV result file
router.get("/api/csv/:fileName", getCsv);

// GET /thumbnail/:fileName - Generate and retrieve a thumbnail for a video
router.get("/thumbnail/:fileName", getThumbnail);

// POST /process/:fileName - Start processing a video file (requires targetColor and threshold query params)
router.post("/process/:fileName", startProcessVideo);

// GET /process/:jobId/status - Check the status of a processing job
router.get("/process/:jobId/status", getStatus);

// Export the configured router for use in the main application
export default router;

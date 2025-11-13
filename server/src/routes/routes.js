import { Router } from "express";
import {
  getAllResults,
  getAllVideos,
  getStatus,
  getThumbnail,
  startProcessVideo,
} from "./../controllers/controller.js";

const router = Router();

//routes
router.get("/api/videos", getAllVideos);
router.get("/api/results", getAllResults);
router.get("/api/csv/:fileName", getCsv);
router.get("/thumbnail/:fileName", getThumbnail);
router.post("/process/:fileName", startProcessVideo);
router.get("/process/:jobId/status", getStatus);

export default router;

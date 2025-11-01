import {Router} from 'express'
import {getAllVideos, getThumbnail, processVideo, getStatus} from './../controllers/controller.js'

const router = Router()


//routes
router.get("/api/videos", getAllVideos);
router.get('/thumbnail/:filename', getThumbnail);
router.post('/process/:filename', processVideo);
router.get('/process/:jobId/status', getStatus)


export default router
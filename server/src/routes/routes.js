import {Router} from 'express'
import {getAllVideos, getThumbnail, startProcessVideo, getStatus} from './../controllers/controller.js'

const router = Router()


//routes
router.get("/api/videos", getAllVideos);
router.get('/thumbnail/:fileName', getThumbnail);
router.post('/process/:fileName', startProcessVideo);
router.get('/process/:jobId/status', getStatus)


export default router
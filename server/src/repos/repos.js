import schema from './../models/models.js';
import { Op } from "sequelize";


export const createJob = async(jobId, fileName)=> {

  const job = {
    jobId,
    status: "processing",
    outputPath: process.env.RESULTS_DIR + "/" + fileName

  }
  await schema.create(job)


};

export const checkJob = async(jobId)=> {

};
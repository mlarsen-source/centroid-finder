import schema from "./../models/models.js";
import { Op } from "sequelize";

export const createJob = async (jobId, fileName, outputPath) => {
  const job = {
    jobId,
    status: "processing",
    outputPath,
  };

  await schema.create(job);
};

export const checkJob = async (jobId) => {
  try {
    const { status, outputPath } = await schema.findByPk(jobId);

    return {
      status,
      outputPath,
    };
  } catch {
    return { status: "not found" };
  }
};

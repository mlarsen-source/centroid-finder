import schema from "./../models/models.js";

export const createJob = async (jobId, outputPath) => {
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

export const updateStatus = async(jobId, success) =>{
  if(success){ schema.update(
    {
      status: "done"
    },
  {
    where: 
      {jobID: jobId}})
  }

  else { schema.update(
    {
      status: "error"
    },
    {
      where: 
        {jobId: jobId}
    })
  }
}
import schema from "./../models/models.js";

/**
 * Create a new job entry in the database
 * @param {string} jobId - Unique identifier for the job
 * @param {string} outputPath - Path where the result CSV will be saved
 */
export const createJob = async (jobId, outputPath) => {
  // Construct the job object with initial status set to 'processing'
  const job = {
    jobId,
    status: "processing",
    outputPath,
  };

  // Insert the new job record into the database
  await schema.create(job);
};

/**
 * Check the status of a job by its ID
 * @param {string} jobId - Unique identifier for the job
 * @returns {Object} Object containing status and outputPath, or 'not found' status
 */
export const checkJob = async (jobId) => {
  try {
    // Find the job record by primary key (jobId)
    const { status, outputPath } = await schema.findByPk(jobId);

    // Return the job status and output path
    return {
      status,
      outputPath,
    };
  } catch {
    // Return 'not found' if job doesn't exist or query fails
    return { status: "not found" };
  }
};

/**
 * Update the status of a job based on processing success or failure
 * @param {string} jobId - Unique identifier for the job
 * @param {boolean} success - True if processing succeeded, false if it failed
 */
export const updateStatus = async (jobId, success) => {
  // If processing was successful, update status to 'done'
  if (success) {
    schema.update(
      {
        status: "done",
      },
      {
        where: { jobID: jobId },
      }
    );
  }

  // If processing failed, update status to 'error'
  else {
    schema.update(
      {
        status: "error",
      },
      {
        where: { jobId: jobId },
      }
    );
  }
};

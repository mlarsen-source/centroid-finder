import { DataTypes } from 'sequelize';
import sequelize from './../db/connect.js';

// Central job table used by the processor pipeline (jobId == UUID passed to clients)
const schema = sequelize.define('job', {
  jobId: {
    type: DataTypes.STRING,
    primaryKey: true
  },
  status: {
    type: DataTypes.STRING // "processing" | "done" | "error"
  },
  outputPath: {
    type: DataTypes.STRING // absolute path to generated CSV
  }
});

// Ensure sqlite schema is up-to-date when the server boots
await schema.sync({ alter: true });

export default schema;
import { DataTypes } from 'sequelize';
import sequelize from './../db/connect.js';

const schema = sequelize.define('job', {
  jobId: {
    type: DataTypes.STRING,
    primaryKey: true
  },
  status: {
    type: DataTypes.STRING
  },
  outputPath: {
    type: DataTypes.STRING
  }
});

await schema.sync({ alter: true });

export default schema;
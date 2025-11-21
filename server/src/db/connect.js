import { Sequelize } from "sequelize";

// Define the path where the SQLite database file will be stored
const storagePath = "/app/data/app.db";

// Initialize Sequelize with SQLite configuration
const sequelize = new Sequelize({
  dialect: "sqlite", // Use SQLite as the database dialect
  storage: storagePath, // Path to the SQLite database file
  logging: (q) => console.log(`Sequelize query: ${q}`), // Log all SQL queries to console
});

try {
  // Attempt to authenticate the database connection
  await sequelize.authenticate();
  console.log("Connected to SQLite DB!");
} catch (err) {
  // Handle connection errors
  console.log(`Can't connect to the SQLite DB!`);
  console.log(err);
}

// Export the configured Sequelize instance for use throughout the application
export default sequelize;

import cors from "cors";
import express from "express";
import apiRouter from "./routes/routes.js";

// Initialize Express application
const app = express();

// Enable Cross-Origin Resource Sharing (CORS) for all routes
app.use(cors());

// Parse incoming JSON request bodies
app.use(express.json());

//routes...

// Mount the API router at the root path
app.use("/", apiRouter);

// Define the port number for the server
const PORT = 3000;

// Start the server and listen on the specified port
app.listen(PORT, console.log(`Server started on http://localhost:${PORT}`));

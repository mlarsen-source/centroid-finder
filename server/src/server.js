import cors from "cors";
import express from "express";
import apiRouter from "./routes/routes.js";

const app = express();
app.use(cors());
app.use(express.static("public"));
app.use(express.json());

//routes...
app.use("/", apiRouter);

const PORT = 3000;

app.listen(PORT, console.log(`Server started on http://localhost:${PORT}`));

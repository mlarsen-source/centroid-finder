import { Sequelize } from "sequelize";

const storagePath = "/app/data/app.db";

const sequelize = new Sequelize({
  dialect: "sqlite",
  storage: storagePath,
  logging: (q) => console.log(`Sequelize query: ${q}`),
});

try {
  await sequelize.authenticate();
  console.log("Connected to SQLite DB!");
} catch (err) {
  console.log(`Can't connect to the SQLite DB!`);
  console.log(err);
}

export default sequelize;

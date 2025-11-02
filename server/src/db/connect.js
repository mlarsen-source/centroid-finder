import { Sequelize } from "sequelize";
import dotenv from 'dotenv';

//configure env
dotenv.config();

//extract required values from process.env
const { DB_NAME, DB_USER, DB_PASSWORD, DB_HOST, DB_PORT, DB_DIALECT } = process.env;

//configure the connection
const sequelize = new Sequelize(DB_NAME, DB_USER, DB_PASSWORD, {
    host: DB_HOST,
    port: DB_PORT,
    dialect: DB_DIALECT,
    logging: q => console.log(`Sequelize query: ${q}`)
});

//attempt a connection
try {
    await sequelize.authenticate();
    console.log('Connected to MySQL DB!');
}
catch (err) {
    console.log(`Can't connect to the DB!`);
    console.log(err);
}

export default sequelize;
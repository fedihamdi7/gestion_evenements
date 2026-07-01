import { NestFactory } from '@nestjs/core';
import { createConnection } from 'mysql2/promise';
import { AppModule } from './app.module';
import { registerWithEureka } from './eureka';

// TypeORM won't create the database itself, so (like the Spring services'
// createDatabaseIfNotExist=true) we create db_messaging up front.
async function ensureDatabase() {
  const conn = await createConnection({
    host: process.env.MYSQL_HOST || 'localhost',
    port: 3306,
    user: process.env.MYSQL_USER || 'root',
    password: process.env.MYSQL_PASSWORD || 'root',
  });
  await conn.query('CREATE DATABASE IF NOT EXISTS db_messaging');
  await conn.end();
}

async function bootstrap() {
  await ensureDatabase();

  const app = await NestFactory.create(AppModule);
  // Let the Angular frontend (http://localhost:4200) call the REST + WebSocket API.
  app.enableCors({ origin: '*' });

  const port = Number(process.env.PORT || 8085);
  await app.listen(port);
  console.log(`service-messaging (REST + WebSocket) listening on ${port}`);

  // Register in Eureka so the API Gateway can route lb://service-messaging.
  registerWithEureka();
}
bootstrap();

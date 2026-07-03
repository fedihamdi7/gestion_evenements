import { NestFactory } from '@nestjs/core';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
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
  // NOTE: no app.enableCors() here — all traffic goes through the API Gateway, which
  // adds the CORS headers. Adding them here too produced DUPLICATE
  // Access-Control-Allow-Origin headers, which browsers reject (chat history failed).
  // (The Socket.IO handshake CORS is configured separately in the WebSocket gateway.)

  // Swagger/OpenAPI — the JSON is served at /api/messages/v3/api-docs so the API Gateway
  // can aggregate it in its Swagger UI dropdown, like the Java services.
  const swaggerConfig = new DocumentBuilder()
    .setTitle('Service Messaging API')
    .setDescription('Instant messaging (REST + Socket.IO)')
    .setVersion('1.0')
    .addServer('/')
    .addBearerAuth()
    .build();
  const document = SwaggerModule.createDocument(app, swaggerConfig);
  SwaggerModule.setup('api/messages/swagger-ui', app, document, {
    jsonDocumentUrl: 'api/messages/v3/api-docs',
  });

  const port = Number(process.env.PORT || 8085);
  await app.listen(port);
  console.log(`service-messaging (REST + WebSocket) listening on ${port}`);

  // Register in Eureka so the API Gateway can route lb://service-messaging.
  registerWithEureka();
}
bootstrap();

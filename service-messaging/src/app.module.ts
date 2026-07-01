import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AppController } from './app.controller';
import { Message } from './messages/message.entity';
import { MessagesModule } from './messages/messages.module';

@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: 'mysql',
      host: process.env.MYSQL_HOST || 'localhost',
      port: 3306,
      username: process.env.MYSQL_USER || 'root',
      password: process.env.MYSQL_PASSWORD || 'root',
      database: 'db_messaging',
      entities: [Message],
      synchronize: true, // dev only: auto-creates/updates the "messages" table
    }),
    MessagesModule,
  ],
  controllers: [AppController],
})
export class AppModule {}

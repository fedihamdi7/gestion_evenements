import { Controller, Get, Query } from '@nestjs/common';
import { MessagesService } from './messages.service';

// Mapped under /api/messages so the API Gateway route Path=/api/messages/**
// forwards here unchanged (the gateway does not strip the prefix).
@Controller('api/messages')
export class MessagesController {
  constructor(private readonly svc: MessagesService) {}

  // GET /api/messages/conversation?user1=a@x.com&user2=b@x.com
  @Get('conversation')
  conversation(@Query('user1') user1: string, @Query('user2') user2: string) {
    return this.svc.conversation(user1, user2);
  }

  // GET /api/messages/contacts?me=a@x.com
  @Get('contacts')
  contacts(@Query('me') me: string) {
    return this.svc.contacts(me);
  }
}

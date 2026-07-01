import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Message } from './message.entity';

@Injectable()
export class MessagesService {
  constructor(
    @InjectRepository(Message) private readonly repo: Repository<Message>,
  ) {}

  save(sender: string, receiver: string, content: string): Promise<Message> {
    return this.repo.save(this.repo.create({ sender, receiver, content }));
  }

  /** Full history between two users, oldest first. */
  conversation(user1: string, user2: string): Promise<Message[]> {
    return this.repo.find({
      where: [
        { sender: user1, receiver: user2 },
        { sender: user2, receiver: user1 },
      ],
      order: { createdAt: 'ASC' },
    });
  }

  /** Distinct people "me" has exchanged messages with, most recent first. */
  async contacts(me: string): Promise<string[]> {
    const rows = await this.repo.find({
      where: [{ sender: me }, { receiver: me }],
      order: { createdAt: 'DESC' },
    });
    const seen = new Set<string>();
    for (const m of rows) seen.add(m.sender === me ? m.receiver : m.sender);
    return [...seen];
  }
}

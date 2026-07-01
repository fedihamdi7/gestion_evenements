import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn } from 'typeorm';

@Entity('messages')
export class Message {
  @PrimaryGeneratedColumn()
  id: number;

  // We identify users by their email (present in both the JWT and /api/users).
  @Column()
  sender: string;

  @Column()
  receiver: string;

  @Column('text')
  content: string;

  @CreateDateColumn()
  createdAt: Date;
}

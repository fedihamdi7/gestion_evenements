import {
  OnGatewayConnection,
  OnGatewayDisconnect,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { MessagesService } from './messages.service';

/**
 * Real-time delivery over Socket.IO.
 *
 * - On connect, the client sends its email (handshake auth) and is put into a
 *   "room" named by that email.
 * - On "send_message", we persist the message then emit "new_message" to the
 *   receiver's room AND the sender's room (so both windows update instantly).
 */
@WebSocketGateway({ cors: { origin: '*' } })
export class MessagesGateway
  implements OnGatewayConnection, OnGatewayDisconnect
{
  @WebSocketServer() server: Server;

  // socket.id -> email
  private readonly online = new Map<string, string>();

  constructor(private readonly svc: MessagesService) {}

  private emailOf(client: Socket): string | undefined {
    return (
      (client.handshake.auth?.email as string) ||
      (client.handshake.query?.email as string) ||
      this.online.get(client.id)
    );
  }

  handleConnection(client: Socket) {
    const email = this.emailOf(client);
    if (email) {
      this.online.set(client.id, email);
      client.join(email);
    }
  }

  handleDisconnect(client: Socket) {
    this.online.delete(client.id);
  }

  @SubscribeMessage('send_message')
  async onSend(client: Socket, payload: { to: string; content: string }) {
    const from = this.emailOf(client);
    if (!from || !payload?.to || !payload?.content?.trim()) return;

    const saved = await this.svc.save(from, payload.to, payload.content.trim());
    this.server.to(payload.to).emit('new_message', saved);
    this.server.to(from).emit('new_message', saved);
    return saved;
  }
}

import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { io, Socket } from 'socket.io-client';
import { environment } from '../../environments/environment';
import { ChatMessage } from './models';

/**
 * Talks to service-messaging DIRECTLY (same host as the realtime socket, :8085):
 *  - REST for conversation history + contacts
 *  - Socket.IO for real-time send/receive
 * We bypass the API Gateway here so history + live chat always work together
 * (the gateway route for /api/messages still exists, but the frontend doesn't depend on it).
 */
@Injectable({ providedIn: 'root' })
export class MessagingService {
  private http = inject(HttpClient);
  private base = `${environment.messagingWsUrl}/api/messages`;
  private socket?: Socket;

  /** fires once per message pushed by the server (incoming + the echo of what we sent) */
  readonly messages$ = new Subject<ChatMessage>();
  readonly connected = signal(false);

  /** Open the realtime connection, identifying ourselves by email. */
  connect(email: string): void {
    if (this.socket) return;
    this.socket = io(environment.messagingWsUrl, {
      auth: { email },
      transports: ['websocket', 'polling'],
    });
    this.socket.on('connect', () => this.connected.set(true));
    this.socket.on('disconnect', () => this.connected.set(false));
    this.socket.on('new_message', (m: ChatMessage) => this.messages$.next(m));
  }

  disconnect(): void {
    this.socket?.disconnect();
    this.socket = undefined;
    this.connected.set(false);
  }

  /** Send a message in real time; the server persists it and echoes it back. */
  send(to: string, content: string): void {
    this.socket?.emit('send_message', { to, content });
  }

  conversation(user1: string, user2: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.base}/conversation`, {
      params: { user1, user2 },
    });
  }

  contacts(me: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/contacts`, { params: { me } });
  }
}

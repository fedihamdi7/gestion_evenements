import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/auth';
import { UserService } from '../../core/users';
import { MessagingService } from '../../core/messaging';
import { ChatMessage, User } from '../../core/models';

/**
 * Instant messaging page. Pick a user on the left, chat on the right.
 * History is loaded via REST (through the gateway); new messages arrive/are sent
 * in real time over the Socket.IO connection.
 */
@Component({
  selector: 'app-chat',
  imports: [FormsModule, DatePipe],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class ChatPage implements OnInit, OnDestroy {
  private auth = inject(AuthService);
  private userService = inject(UserService);
  private messaging = inject(MessagingService);

  readonly me = this.auth.currentEmail();
  readonly connected = this.messaging.connected;

  users = signal<User[]>([]);
  selected = signal<User | null>(null);
  messages = signal<ChatMessage[]>([]);
  draft = signal('');
  loading = signal(false);
  // emails of people who messaged me in a thread I don't have open (unread badges)
  unread = signal<Set<string>>(new Set());

  // the scrollable messages container, so we can keep it pinned to the latest message
  private messagesBox = viewChild<ElementRef<HTMLDivElement>>('messagesBox');

  private sub?: Subscription;

  /** Scroll the conversation to the newest message (deferred until the DOM has rendered). */
  private scrollToBottom() {
    setTimeout(() => {
      const el = this.messagesBox()?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }

  ngOnInit() {
    if (this.me) this.messaging.connect(this.me);

    // Append realtime messages that belong to the currently open conversation.
    this.sub = this.messaging.messages$.subscribe((m) => this.onIncoming(m));

    this.userService.findAll().subscribe({
      next: (list) => {
        this.users.set(list.filter((u) => u.email !== this.me));
        // Re-open the last conversation after a refresh so its history reloads
        // (SPA state is lost on refresh; the messages are still in the DB).
        const savedEmail = sessionStorage.getItem('chat_selected');
        if (savedEmail) {
          const u = this.users().find((x) => x.email === savedEmail);
          if (u) this.openChat(u);
        }
      },
      error: () => {},
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
    this.messaging.disconnect();
  }

  openChat(u: User) {
    this.selected.set(u);
    sessionStorage.setItem('chat_selected', u.email); // remember across refresh (per-tab)
    this.messages.set([]);
    // clear the unread badge for this person
    this.unread.update((s) => {
      const n = new Set(s);
      n.delete(u.email);
      return n;
    });
    if (!this.me) return;
    this.loading.set(true);
    this.messaging.conversation(this.me, u.email).subscribe({
      next: (list) => {
        this.messages.set(list);
        this.loading.set(false);
        this.scrollToBottom(); // jump to the latest message when opening a chat
      },
      error: () => this.loading.set(false),
    });
  }

  send() {
    const text = this.draft().trim();
    const other = this.selected();
    if (!text || !other) return;
    this.messaging.send(other.email, text); // the echo will append it to the list
    this.draft.set('');
  }

  isMine(m: ChatMessage) {
    return m.sender === this.me;
  }

  hasUnread(email: string) {
    return this.unread().has(email);
  }

  private onIncoming(m: ChatMessage) {
    if (!this.me) return;
    const other = this.selected()?.email;
    const belongsToOpen =
      !!other &&
      ((m.sender === this.me && m.receiver === other) ||
        (m.sender === other && m.receiver === this.me));

    if (belongsToOpen) {
      this.messages.update((list) => [...list, m]);
      this.scrollToBottom(); // stay pinned to the newest message
    } else if (m.receiver === this.me) {
      // a message arrived for a conversation I don't have open -> badge the sender
      this.unread.update((s) => {
        const n = new Set(s);
        n.add(m.sender);
        return n;
      });
    }
  }
}

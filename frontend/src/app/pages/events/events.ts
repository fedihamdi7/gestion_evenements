import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth';
import { UserService } from '../../core/users';
import { EventsApiService } from '../../core/events-api';
import { Avis, Event, Reservation, User } from '../../core/models';

/**
 * Full demo flow, all through the API Gateway:
 *   list events -> open one -> book it (reservation) -> see who booked ->
 *   bookers can leave a rating (avis). Touches service-evenements,
 *   service-reservation and service-avis.
 */
@Component({
  selector: 'app-events',
  imports: [FormsModule, DatePipe],
  templateUrl: './events.html',
  styleUrl: './events.css',
})
export class EventsPage implements OnInit {
  private auth = inject(AuthService);
  private userService = inject(UserService);
  private api = inject(EventsApiService);

  private readonly myEmail = this.auth.currentEmail();

  users = signal<User[]>([]);
  myUserId = signal<number | null>(null);

  events = signal<Event[]>([]);
  selected = signal<Event | null>(null);
  bookers = signal<Reservation[]>([]);
  ratings = signal<Avis[]>([]);
  message = signal<{ text: string; ok: boolean } | null>(null);

  // rating form
  note = signal(5);
  commentaire = signal('');

  readonly average = computed(() => {
    const r = this.ratings();
    if (!r.length) return 0;
    return Math.round((r.reduce((s, a) => s + a.note, 0) / r.length) * 10) / 10;
  });

  readonly iBooked = computed(() =>
    this.bookers().some((b) => b.userId === this.myUserId()),
  );

  ngOnInit() {
    // resolve my numeric user id (reservations/avis use it) from my email
    this.userService.findAll().subscribe({
      next: (list) => {
        this.users.set(list);
        const me = list.find((u) => u.email === this.myEmail);
        this.myUserId.set(me ? me.id : null);
      },
      error: () => {},
    });
    this.api.events().subscribe({
      next: (list) => this.events.set(list),
      error: () => this.message.set({ text: 'Chargement des événements échoué.', ok: false }),
    });
  }

  openEvent(e: Event) {
    this.selected.set(e);
    this.message.set(null);
    this.loadBookers(e.id);
    this.loadRatings(e.id);
  }

  private loadBookers(eventId: string) {
    this.api.reservationsByEvent(eventId).subscribe({
      next: (list) => this.bookers.set(list),
      error: () => this.bookers.set([]),
    });
  }
  private loadRatings(eventId: string) {
    this.api.ratingsByEvent(eventId).subscribe({
      next: (list) => this.ratings.set(list),
      error: () => this.ratings.set([]),
    });
  }

  book() {
    const e = this.selected();
    const uid = this.myUserId();
    if (!e || uid == null) return;
    const reservation: Reservation = {
      userId: uid,
      eventId: e.id,
      eventTitle: e.title,
      eventDate: e.date,
      status: 'CONFIRMED',
    };
    this.api.book(reservation).subscribe({
      next: () => {
        this.message.set({ text: `Réservation confirmée pour "${e.title}".`, ok: true });
        this.loadBookers(e.id);
      },
      error: () => this.message.set({ text: 'Réservation échouée.', ok: false }),
    });
  }

  submitRating() {
    const e = this.selected();
    const uid = this.myUserId();
    if (!e || uid == null) return;
    const avis: Avis = {
      utilisateurId: uid,
      evenementId: e.id,
      note: this.note(),
      commentaire: this.commentaire().trim(),
    };
    this.api.addRating(avis).subscribe({
      next: () => {
        this.message.set({ text: 'Merci pour votre avis !', ok: true });
        this.commentaire.set('');
        this.note.set(5);
        this.loadRatings(e.id);
      },
      error: () => this.message.set({ text: 'Avis échoué.', ok: false }),
    });
  }

  userName(userId: number): string {
    const u = this.users().find((x) => x.id === userId);
    return u ? `${u.prenom} ${u.nom}` : `Utilisateur #${userId}`;
  }

  stars(n: number): string {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  }
}

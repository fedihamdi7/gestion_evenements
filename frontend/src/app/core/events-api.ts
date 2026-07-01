import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Avis, Event, Reservation } from './models';

/**
 * All event / reservation / rating calls — every one goes THROUGH the API Gateway
 * (environment.apiUrl = :9090). The auth interceptor attaches the Keycloak JWT.
 */
@Injectable({ providedIn: 'root' })
export class EventsApiService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  // ---- events (service-evenements) ----
  events(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.api}/api/events`);
  }
  event(id: string): Observable<Event> {
    return this.http.get<Event>(`${this.api}/api/events/${id}`);
  }

  // ---- reservations (service-reservation) ----
  book(reservation: Reservation): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.api}/api/reservations`, reservation);
  }
  reservationsByEvent(eventId: string): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.api}/api/reservations/event/${eventId}`);
  }

  // ---- ratings (service-avis) ----
  addRating(avis: Avis): Observable<Avis> {
    return this.http.post<Avis>(`${this.api}/api/avis`, avis);
  }
  ratingsByEvent(eventId: string): Observable<Avis[]> {
    return this.http.get<Avis[]>(`${this.api}/api/avis/evenement/${eventId}`);
  }
}

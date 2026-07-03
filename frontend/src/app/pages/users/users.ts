import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../core/users';
import { AuthService } from '../../core/auth';
import { ROLES, Role, User } from '../../core/models';

type Msg = { text: string; ok: boolean } | null;

@Component({
  selector: 'app-users',
  imports: [FormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class UsersPage implements OnInit {
  private service = inject(UserService);
  private auth = inject(AuthService);

  /** the logged-in admin's own email — used to disable self-management actions. */
  readonly myEmail = this.auth.currentEmail();
  readonly roles = ROLES;

  isSelf(u: User): boolean {
    return !!this.myEmail && u.email?.toLowerCase() === this.myEmail.toLowerCase();
  }
  users = signal<User[]>([]);
  loading = signal(false);
  message = signal<Msg>(null);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.service.findAll().subscribe({
      next: (list) => {
        this.users.set(list);
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.message.set({ text: 'Chargement échoué : ' + this.errMsg(e), ok: false });
      },
    });
  }

  saveRole(u: User, role: Role) {
    this.service.updateRole(u.id, role).subscribe({
      next: () => this.message.set({ text: `Rôle de ${u.email} → ${role}`, ok: true }),
      error: (e) => this.message.set({ text: 'Mise à jour échouée : ' + this.errMsg(e), ok: false }),
    });
  }

  remove(u: User) {
    if (!confirm(`Supprimer ${u.email} ? Il sera retiré de Keycloak aussi.`)) return;
    this.service.remove(u.id).subscribe({
      next: () => {
        this.message.set({ text: `${u.email} supprimé.`, ok: true });
        this.load();
      },
      error: (e) => this.message.set({ text: 'Suppression échouée : ' + this.errMsg(e), ok: false }),
    });
  }

  private errMsg(e: HttpErrorResponse): string {
    if (e.error?.message) return e.error.message;
    if (e.status === 0) return 'API Gateway injoignable (port 9090).';
    if (e.status === 401) return 'Session expirée, reconnectez-vous.';
    return `Erreur ${e.status}.`;
  }
}

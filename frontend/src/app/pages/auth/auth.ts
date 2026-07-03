import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth';
import { ROLES, RegisterRequest, Role } from '../../core/models';

type Tab = 'login' | 'register';
type Msg = { text: string; ok: boolean } | null;

@Component({
  selector: 'app-auth',
  imports: [FormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css',
})
export class AuthPage {
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly roles = ROLES;
  tab = signal<Tab>('login');
  message = signal<Msg>(null);
  loading = signal(false);

  // login model
  loginEmail = 'fedi@esprit.tn';
  loginPass = 'password';

  // register model
  reg: RegisterRequest = { nom: '', prenom: '', email: '', motDePasse: '', role: 'PARTICIPANT' };

  switchTab(t: Tab) {
    this.tab.set(t);
    this.message.set(null);
  }

  login() {
    this.loading.set(true);
    this.auth.login({ email: this.loginEmail, motDePasse: this.loginPass }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/events']);
      },
      error: (e) => {
        this.loading.set(false);
        this.message.set({ text: 'Connexion échouée : ' + this.errMsg(e), ok: false });
      },
    });
  }

  register() {
    this.loading.set(true);
    this.auth.register(this.reg).subscribe({
      next: (u) => {
        this.loading.set(false);
        this.message.set({ text: `Compte créé pour ${u.email} (id ${u.id}). Connectez-vous.`, ok: true });
        // pre-fill login with the new account and switch tab
        this.loginEmail = u.email;
        this.loginPass = this.reg.motDePasse;
        this.tab.set('login');
      },
      error: (e) => {
        this.loading.set(false);
        this.message.set({ text: 'Inscription échouée : ' + this.errMsg(e), ok: false });
      },
    });
  }

  private errMsg(e: HttpErrorResponse): string {
    if (e.error?.message) return e.error.message;
    if (e.status === 0) return 'API Gateway injoignable (port 9090).';
    if (e.status === 401) return 'Identifiants invalides.';
    if (e.status === 409) return 'Email déjà utilisé.';
    if (e.status === 400) return 'Données invalides.';
    return `Erreur ${e.status}.`;
  }
}

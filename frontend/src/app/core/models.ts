// Shared types that mirror the service-utilisateurs DTOs.

export type Role = 'ADMIN' | 'ORGANISATEUR' | 'PARTICIPANT';

export const ROLES: Role[] = ['PARTICIPANT', 'ORGANISATEUR', 'ADMIN'];

export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  motDePasse: string;
}

// POST /api/users/login returns the Keycloak token
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

// A chat message from service-messaging (users identified by email).
export interface ChatMessage {
  id: number;
  sender: string;
  receiver: string;
  content: string;
  createdAt: string;
}

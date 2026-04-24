import { Injectable, computed, signal } from '@angular/core';

export type UserRole = 'USER' | 'TALLER' | 'ADMIN';

const AVAILABLE_ROLES: UserRole[] = ['USER', 'TALLER', 'ADMIN'];

interface AuthSession {
  userId: number | null;
  loggedIn: boolean;
  email: string;
  userName: string;
  userAvatar: string;
  role: UserRole | null;
}

const STORAGE_KEY = 'autodiagnostico.auth';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly session = signal<AuthSession>(this.loadSession());

  readonly isLoggedIn = computed(() => this.session().loggedIn);
  readonly userName = computed(() => this.session().userName);
  readonly userAvatar = computed(() => this.session().userAvatar);
  readonly role = computed(() => this.session().role);
  readonly userId = computed(() => this.session().userId);
  readonly email = computed(() => this.session().email);
  readonly canAccessSeguimiento = computed(() => {
    const session = this.session();
    return session.loggedIn && session.role !== null && AVAILABLE_ROLES.includes(session.role);
  });

  applyAuthenticatedUser(user: { id: number; fullName: string; email: string; role: UserRole; avatarUrl: string }): void {
    const normalizedName = user.fullName.trim();

    this.setSession({
      userId: user.id,
      loggedIn: true,
      email: user.email,
      userName: normalizedName || this.fallbackNameForRole(user.role),
      userAvatar: user.avatarUrl,
      role: user.role
    });
  }

  setSession(session: Partial<AuthSession>): void {
    const nextSession: AuthSession = {
      userId: session.userId ?? this.session().userId,
      loggedIn: session.loggedIn ?? true,
      email: session.email ?? this.session().email,
      userName: session.userName ?? this.session().userName,
      userAvatar: session.userAvatar ?? this.session().userAvatar,
      role: session.role ?? this.session().role
    };

    this.session.set(nextSession);
    this.persistSession(nextSession);
  }

  clearSession(): void {
    const clearedSession: AuthSession = {
      userId: null,
      loggedIn: false,
      email: '',
      userName: 'Invitado',
      userAvatar: 'https://api.dicebear.com/9.x/initials/svg?seed=AU&backgroundColor=1a6bbd',
      role: null
    };

    this.session.set(clearedSession);
    this.persistSession(clearedSession);
  }

  private loadSession(): AuthSession {
    if (typeof window === 'undefined') {
      return {
        userId: null,
        loggedIn: false,
        email: '',
        userName: 'Invitado',
        userAvatar: 'https://api.dicebear.com/9.x/initials/svg?seed=AU&backgroundColor=1a6bbd',
        role: null
      };
    }

    const storedValue = window.localStorage.getItem(STORAGE_KEY);
    if (!storedValue) {
      return {
        userId: null,
        loggedIn: false,
        email: '',
        userName: 'Invitado',
        userAvatar: 'https://api.dicebear.com/9.x/initials/svg?seed=AU&backgroundColor=1a6bbd',
        role: null
      };
    }

    try {
      const parsedValue = JSON.parse(storedValue) as Partial<AuthSession>;
      return {
        userId: parsedValue.userId ?? null,
        loggedIn: parsedValue.loggedIn ?? false,
        email: parsedValue.email ?? '',
        userName: parsedValue.userName ?? 'Invitado',
        userAvatar: parsedValue.userAvatar ?? 'https://api.dicebear.com/9.x/initials/svg?seed=AU&backgroundColor=1a6bbd',
        role: parsedValue.role ?? null
      };
    } catch {
      return {
        userId: null,
        loggedIn: false,
        email: '',
        userName: 'Invitado',
        userAvatar: 'https://api.dicebear.com/9.x/initials/svg?seed=AU&backgroundColor=1a6bbd',
        role: null
      };
    }
  }

  private persistSession(session: AuthSession): void {
    if (typeof window === 'undefined') {
      return;
    }

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  }

  private fallbackNameForRole(role: UserRole): string {
    if (role === 'TALLER') {
      return 'Taller';
    }

    if (role === 'ADMIN') {
      return 'Administrador';
    }

    return 'Usuario';
  }
}

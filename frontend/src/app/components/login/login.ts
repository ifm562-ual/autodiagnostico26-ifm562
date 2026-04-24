import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthUserRole, AuthUserResponse } from '../../services/api.models';
import { AuthStateService, UserRole } from '../../services/auth-state.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  private readonly authStateService = inject(AuthStateService);
  private readonly authApiService = inject(AuthApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  mode: 'login' | 'register' = 'login';
  fullName = '';
  email = '';
  password = '';
  confirmPassword = '';
  selectedRole: AuthUserRole = 'USER';
  keepSession = true;
  errorMessage = '';
  isSubmitting = false;

  readonly roleCards = [
    {
      role: 'USER' as const,
      title: 'Usuario',
      description: 'Consulta diagnósticos, seguimiento y repuestos.'
    },
    {
      role: 'TALLER' as const,
      title: 'Taller',
      description: 'Responde mensajes, actualiza estados y coordina citas.'
    },
    {
      role: 'ADMIN' as const,
      title: 'Admin',
      description: 'Gestiona accesos y supervisa la actividad del sistema.'
    }
  ];

  ngOnInit(): void {
    this.mode = this.route.snapshot.routeConfig?.path === 'registro' ? 'register' : 'login';
  }

  switchMode(nextMode: 'login' | 'register'): void {
    this.mode = nextMode;
    this.errorMessage = '';
    void this.router.navigate([nextMode === 'login' ? '/login' : '/registro']);
  }

  submit(): void {
    if (this.isSubmitting) {
      return;
    }

    if (this.mode === 'register') {
      this.submitRegister();
      return;
    }

    this.submitLogin();
  }

  private submitLogin(): void {
    const email = this.email.trim();
    if (!email) {
      this.errorMessage = 'Escribe tu correo para continuar.';
      return;
    }

    if (!this.password.trim()) {
      this.errorMessage = 'Escribe tu contraseña para continuar.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.authApiService.login({ email, password: this.password }).subscribe({
      next: (user) => this.completeSession(user),
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = this.extractErrorMessage(error, 'No se pudo iniciar sesión.');
      }
    });
  }

  private submitRegister(): void {
    const fullName = this.fullName.trim();
    const email = this.email.trim();
    const password = this.password.trim();
    const confirmPassword = this.confirmPassword.trim();

    if (!fullName) {
      this.errorMessage = 'Escribe tu nombre completo para crear la cuenta.';
      return;
    }

    if (!email) {
      this.errorMessage = 'Escribe tu correo para crear la cuenta.';
      return;
    }

    if (!password) {
      this.errorMessage = 'Escribe una contraseña para crear la cuenta.';
      return;
    }

    if (password.length < 6) {
      this.errorMessage = 'La contraseña debe tener al menos 6 caracteres.';
      return;
    }

    if (password !== confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.authApiService.register({ fullName, email, password, role: this.selectedRole }).subscribe({
      next: (user) => this.completeSession(user),
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = this.extractErrorMessage(error, 'No se pudo crear la cuenta.');
      }
    });
  }

  private completeSession(user: AuthUserResponse): void {
    this.authStateService.applyAuthenticatedUser({
      id: user.id,
      fullName: user.fullName,
      email: user.email,
      role: user.role as UserRole,
      avatarUrl: user.avatarUrl
    });

    this.isSubmitting = false;
    const redirectToSeguimiento = this.mode === 'register' || user.role === 'ADMIN';
    void this.router.navigate([redirectToSeguimiento ? '/seguimiento' : '/home']);
  }

  private extractErrorMessage(error: unknown, fallbackMessage: string): string {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const responseError = error as { error?: { message?: string } | string };
      if (typeof responseError.error === 'string') {
        return responseError.error;
      }
      if (responseError.error && typeof responseError.error === 'object' && 'message' in responseError.error) {
        return responseError.error.message ?? fallbackMessage;
      }
    }

    return fallbackMessage;
  }
}
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { AuthLoginRequest, AuthRegisterRequest, AuthUserResponse } from './api.models';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly baseUrl = `${API_BASE_URL}/auth`;

  constructor(private readonly http: HttpClient) {}

  login(payload: AuthLoginRequest): Observable<AuthUserResponse> {
    return this.http.post<AuthUserResponse>(`${this.baseUrl}/login`, payload);
  }

  register(payload: AuthRegisterRequest): Observable<AuthUserResponse> {
    return this.http.post<AuthUserResponse>(`${this.baseUrl}/register`, payload);
  }
}

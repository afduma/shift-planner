import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { UsersApiService } from '../api/users-api.service';
import { API_BASE_URL } from '../config/api.config';
import { User } from '../models/user.model';
import { LoginRequest, LoginResponse } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly usersApi = inject(UsersApiService);
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly storageKey = 'shift-planner.access-token';

  private readonly tokenState = signal<string | null>(localStorage.getItem(this.storageKey));
  private readonly currentUserState = signal<User | null>(null);

  readonly currentUser = computed(() => this.currentUserState());
  readonly token = computed(() => this.tokenState());

  login(email: string, password: string): Observable<User> {
    const payload: LoginRequest = { email, password };

    return this.http.post<LoginResponse>(`${this.apiBaseUrl}/auth/login`, payload).pipe(
      tap((response) => this.setToken(response.accessToken)),
      switchMap(() => this.usersApi.getCurrentUser()),
      tap((user) => {
        this.currentUserState.set(user);
        void this.router.navigate(['/dashboard']);
      }),
    );
  }

  logout(options?: { redirect?: boolean }): void {
    localStorage.removeItem(this.storageKey);
    this.tokenState.set(null);
    this.currentUserState.set(null);

    if (options?.redirect !== false) {
      void this.router.navigate(['/login']);
    }
  }

  getCurrentUser(): Observable<User> {
    return this.usersApi.getCurrentUser().pipe(tap((user) => this.currentUserState.set(user)));
  }

  restoreSession(): Observable<User | null> {
    if (!this.tokenState()) {
      return of(null);
    }

    return this.getCurrentUser().pipe(
      map((user) => user),
      catchError((error) => {
        this.logout({ redirect: false });
        return throwError(() => error);
      }),
    );
  }

  isAuthenticated(): boolean {
    return this.tokenState() !== null;
  }

  getAccessToken(): string | null {
    return this.tokenState();
  }

  private setToken(token: string): void {
    localStorage.setItem(this.storageKey, token);
    this.tokenState.set(token);
  }
}

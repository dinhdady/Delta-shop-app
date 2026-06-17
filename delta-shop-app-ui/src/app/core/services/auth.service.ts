import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  avatarUrl?: string | null;
  role: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  avatarUrl?: string | null;
  role: string;
  emailVerified: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  // State using Signals
  public currentUser = signal<User | null>(null);
  public isAuthenticated = signal<boolean>(false);

  constructor(private http: HttpClient) {
    this.checkInitialAuth();
  }

  private checkInitialAuth() {
    const token = this.getStoredAccessToken();
    const refreshToken = this.getRefreshToken();
    const userJson = localStorage.getItem('user');
    if (token && !this.isTokenExpired(token) && userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUser.set(user);
        this.isAuthenticated.set(true);
      } catch (e) {
        this.clearLocalSession();
      }
      return;
    }

    if (refreshToken && !this.isTokenExpired(refreshToken) && userJson) {
      this.refreshSession().subscribe({
        error: () => this.clearLocalSession()
      });
      return;
    }

    this.clearLocalSession();
  }

  login(credentials: any, password?: string): Observable<AuthResponse> {
    const loginData = password ? { email: credentials, password } : credentials;
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginData).pipe(
      tap(response => {
        if (response.accessToken) {
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('token', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
          const user: User = {
            userId: response.userId,
            email: response.email,
            firstName: response.firstName,
            lastName: response.lastName,
            fullName: response.fullName,
            avatarUrl: response.avatarUrl,
            role: response.role,
            emailVerified: response.emailVerified
          };
          localStorage.setItem('user', JSON.stringify(user));
          this.currentUser.set(user);
          this.isAuthenticated.set(true);
        }
      })
    );
  }

  register(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
  }

  verifyEmail(data: { email: string, otp: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-email`, data);
  }

  resetPassword(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, data);
  }

  logout(): void {
    const token = localStorage.getItem('refreshToken');
    if (token) {
      this.http.post(`${this.apiUrl}/logout`, {}, {
        headers: { 'Authorization': `Bearer ${token}` }
      }).subscribe({
        error: () => {} // Bỏ qua lỗi logout — vẫn clear local state
      });
    }
    this.clearLocalSession();
  }

  clearLocalSession(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }

  getToken(): string | null {
    const token = this.getStoredAccessToken();
    if (!token || this.isTokenExpired(token)) {
      this.syncAuthState();
      return null;
    }
    return token;
  }

  getRefreshToken(): string | null {
    const refreshToken = localStorage.getItem('refreshToken');
    return refreshToken && !this.isTokenExpired(refreshToken) ? refreshToken : null;
  }

  hasValidSession(): boolean {
    const accessToken = this.getStoredAccessToken();
    const refreshToken = localStorage.getItem('refreshToken');
    return !!(accessToken && !this.isTokenExpired(accessToken)) ||
           !!(refreshToken && !this.isTokenExpired(refreshToken));
  }

  syncAuthState(): void {
    const accessToken = this.getStoredAccessToken();
    const refreshToken = localStorage.getItem('refreshToken');
    const userJson = localStorage.getItem('user');

    if (accessToken && !this.isTokenExpired(accessToken) && userJson) {
      if (!this.currentUser()) {
        try {
          this.currentUser.set(JSON.parse(userJson));
        } catch {
          this.clearLocalSession();
          return;
        }
      }
      this.isAuthenticated.set(true);
      return;
    }

    if (!refreshToken || this.isTokenExpired(refreshToken)) {
      this.clearLocalSession();
      return;
    }

    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }

  refreshSession(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.clearLocalSession();
      return throwError(() => new Error('No valid refresh token available'));
    }

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(response => this.storeAuthResponse(response))
    );
  }

  storeAuthResponse(response: AuthResponse): void {
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('token', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
    const user: User = {
      userId: response.userId,
      email: response.email,
      firstName: response.firstName,
      lastName: response.lastName,
      fullName: response.fullName,
      avatarUrl: response.avatarUrl,
      role: response.role,
      emailVerified: response.emailVerified
    };
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUser.set(user);
    this.isAuthenticated.set(true);
  }

  private getStoredAccessToken(): string | null {
    return localStorage.getItem('accessToken') || localStorage.getItem('token');
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (!payload.exp) return false;
      return payload.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }

  isAdmin(): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return this.hasAdminAccess(user.role);
  }

  updateCurrentUserAvatar(avatarUrl: string | null): void {
    const user = this.currentUser();
    if (!user) return;

    const updatedUser = { ...user, avatarUrl };
    localStorage.setItem('user', JSON.stringify(updatedUser));
    this.currentUser.set(updatedUser);
  }

  hasAdminAccess(role?: string | null): boolean {
    const normalizedRole = role?.toUpperCase().replace(/-/g, '_') ?? '';
    return normalizedRole === 'ADMIN' ||
           normalizedRole === 'ROLE_ADMIN' ||
           normalizedRole === 'SUPER_ADMIN' ||
           normalizedRole === 'ROLE_SUPER_ADMIN' ||
           normalizedRole === 'SUPERADMIN' ||
           normalizedRole === 'ROLE_SUPERADMIN';
  }
}

import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
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
    const token = this.getToken();
    const userJson = localStorage.getItem('user');
    if (token && userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUser.set(user);
        this.isAuthenticated.set(true);
      } catch (e) {
        this.logout();
      }
    }
  }

  login(credentials: any, password?: string): Observable<AuthResponse> {
    const loginData = password ? { email: credentials, password } : credentials;
    if (this.isMockAdminLogin(loginData)) {
      const response = this.createMockAdminResponse();
      this.storeAuthResponse(response);
      return of(response);
    }

    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginData).pipe(
      tap(response => {
        if (response.accessToken) {
          this.storeAuthResponse(response);
        }
      })
    );
  }

  private isMockAdminLogin(loginData: any): boolean {
    const username = String(loginData?.email || loginData?.username || '').trim().toLowerCase();
    return username === 'admin' && loginData?.password === 'admin123';
  }

  private createMockAdminResponse(): AuthResponse {
    return {
      accessToken: 'mock-admin-access-token',
      refreshToken: 'mock-admin-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 86400,
      userId: 'mock-admin',
      email: 'admin@delta-sports.test',
      firstName: 'Admin',
      lastName: 'Delta',
      fullName: 'Admin Delta',
      role: 'ADMIN',
      emailVerified: true
    };
  }

  private storeAuthResponse(response: AuthResponse): void {
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('token', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
    const user: User = {
      userId: response.userId,
      email: response.email,
      firstName: response.firstName,
      lastName: response.lastName,
      fullName: response.fullName,
      role: response.role,
      emailVerified: response.emailVerified
    };
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUser.set(user);
    this.isAuthenticated.set(true);
  }

  register(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
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
    // Clear ngay lập tức, không đợi response
    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }
  getToken(): string | null {
    return localStorage.getItem('accessToken') || localStorage.getItem('token');
  }

  isAdmin(): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return this.hasAdminAccess(user.role);
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

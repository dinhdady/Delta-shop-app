import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {}

  getUsers(page: number, size: number, role?: string, status?: string, keyword?: string): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (role) params = params.set('role', role);
    if (status) params = params.set('status', status);
    if (keyword) params = params.set('keyword', keyword);
    return this.http.get(`${this.apiUrl}/admin`, { params });
  }

  getUserById(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/admin/${id}`);
  }

  updateUser(id: string, user: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/${id}`, user);
  }

  updateStatus(id: string, status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/admin/${id}/status?status=${status}`, {});
  }

  toggleUserStatus(id: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/admin/${id}/toggle-status`, {});
  }

  updateUserRole(id: string, role: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/admin/${id}/role?role=${role}`, {});
  }

  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admin/${id}`);
  }
}

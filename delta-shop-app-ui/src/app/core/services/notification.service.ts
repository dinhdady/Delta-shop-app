import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { PageResponse } from './product.service';

export interface NotificationItem {
  id: string;
  title: string;
  body: string;
  type: string;
  data?: string;
  read: boolean;
  createdAt: string;
  readAt?: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = '/api/notifications';
  unreadCount = signal(0);

  constructor(private http: HttpClient) {}

  loadUnreadCount(): void {
    this.getUnreadCount().subscribe({
      next: response => this.unreadCount.set(response.count || 0),
      error: () => this.unreadCount.set(0)
    });
  }

  getNotifications(page = 0, size = 20): Observable<PageResponse<NotificationItem>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<NotificationItem>>(this.apiUrl, { params });
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`);
  }

  markAsRead(notificationId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${notificationId}/read`, {}).pipe(
      tap(() => this.unreadCount.set(Math.max(0, this.unreadCount() - 1)))
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/read-all`, {}).pipe(
      tap(() => this.unreadCount.set(0))
    );
  }

  delete(notificationId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }

  deleteRead(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/read`);
  }
}

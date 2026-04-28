import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private apiUrl = '/api/categories';

  constructor(private http: HttpClient) {}

  getActiveCategories(): Observable<any> {
    return this.http.get(`${this.apiUrl}/active`);
  }

  getCategoryTree(): Observable<any> {
    return this.http.get(`${this.apiUrl}/tree`);
  }
}

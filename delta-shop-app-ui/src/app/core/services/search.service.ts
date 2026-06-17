import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from './product.service';

export interface AutoCompleteSuggestion {
  id: string;
  name: string;
  slug: string;
  primaryImage: string;
  basePrice: number;
  categoryName: string;
}

export interface SearchSuggestionResponse {
  keyword: string;
  suggestions: AutoCompleteSuggestion[];
  popularKeywords: string[];
}

export interface FilterOption {
  value: string;
  label: string;
  count: number;
}

export interface Facet {
  name: string;
  options: FilterOption[];
}

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  private apiUrl = '/api/public/search';

  constructor(private http: HttpClient) { }

  getAutoComplete(prefix: string, limit: number = 8): Observable<AutoCompleteSuggestion[]> {
    const params = new HttpParams()
      .set('prefix', prefix)
      .set('limit', String(limit));
    return this.http.get<AutoCompleteSuggestion[]>(`${this.apiUrl}/autocomplete`, { params });
  }

  getSuggestions(keyword: string): Observable<SearchSuggestionResponse> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<SearchSuggestionResponse>(`${this.apiUrl}/suggestions`, { params });
  }

  getPopularKeywords(limit: number = 5): Observable<string[]> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<string[]>(`${this.apiUrl}/popular`, { params });
  }

  getFacets(keyword: string): Observable<Facet[]> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<Facet[]>(`${this.apiUrl}/facets`, { params });
  }

  getFilterOptions(categorySlug?: string): Observable<Record<string, FilterOption[]>> {
    let params = new HttpParams();
    if (categorySlug) {
      params = params.set('categorySlug', categorySlug);
    }
    return this.http.get<Record<string, FilterOption[]>>(`${this.apiUrl}/filter-options`, { params });
  }

  advancedSearch(request: any): Observable<PageResponse<any>> {
    return this.http.post<PageResponse<any>>(`${this.apiUrl}/advanced`, request);
  }
}

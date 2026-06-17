import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from './product.service';
import { environment } from '../../../environments/environment';

export interface Review {
  id: string;
  productId: string;
  productName: string;
  userId: string;
  userName: string;
  userAvatar: string;
  rating: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  title: string;
  body: string;
  images: string[];
  verifiedPurchase: boolean;
  helpfulCount: number;
  adminReply: string;
  adminReplyAt: string;
  createdAt: string;
  votedHelpful?: boolean;
}

export interface ReviewEligibility {
  orderItemId: string;
  productId: string | null;
  canReview: boolean;
  reviewed: boolean;
  reason: string | null;
  reviewId: string | null;
  reviewStatus: 'PENDING' | 'APPROVED' | 'REJECTED' | null;
}

export interface ReviewStats {
  averageRating: number;
  totalReviews: number;
  fiveStarCount: number;
  fourStarCount: number;
  threeStarCount: number;
  twoStarCount: number;
  oneStarCount: number;
  verifiedPurchaseCount: number;
  withImagesCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) { }

  getProductReviews(productId: string, page: number = 0, size: number = 10, sortBy: string = 'createdAt'): Observable<PageResponse<Review>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy);
    return this.http.get<PageResponse<Review>>(`${this.apiUrl}/products/${productId}`, { params });
  }

  getProductReviewStats(productId: string): Observable<ReviewStats> {
    return this.http.get<ReviewStats>(`${this.apiUrl}/products/${productId}/stats`);
  }

  createReview(payload: { productId: string; orderItemId: string; rating: number; title?: string; body?: string; images?: string[] }): Observable<Review> {
    return this.http.post<Review>(this.apiUrl, payload);
  }

  getReviewEligibility(orderItemId: string): Observable<ReviewEligibility> {
    return this.http.get<ReviewEligibility>(`${this.apiUrl}/order-items/${orderItemId}/eligibility`);
  }

  getProductReviewEligibility(productId: string): Observable<ReviewEligibility> {
    return this.http.get<ReviewEligibility>(`${this.apiUrl}/eligibility/products/${productId}`);
  }

  updateReview(reviewId: string, payload: { rating?: number; title?: string; body?: string }): Observable<Review> {
    return this.http.put<Review>(`${this.apiUrl}/${reviewId}`, payload);
  }

  deleteReview(reviewId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${reviewId}`);
  }

  voteHelpful(reviewId: string, helpful: boolean): Observable<void> {
    const params = new HttpParams().set('helpful', String(helpful));
    return this.http.post<void>(`${this.apiUrl}/${reviewId}/helpful`, {}, { params });
  }

  getMyReviews(page: number = 0, size: number = 10): Observable<PageResponse<Review>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<PageResponse<Review>>(`${this.apiUrl}/me`, { params });
  }

  getPendingReviews(page: number = 0, size: number = 20): Observable<PageResponse<Review>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<PageResponse<Review>>(`${this.apiUrl}/admin/pending`, { params });
  }

  moderateReview(reviewId: string, payload: { status: string; adminReply?: string }): Observable<Review> {
    return this.http.post<Review>(`${this.apiUrl}/admin/${reviewId}/moderate`, payload);
  }

  batchModerateReviews(reviewIds: string[], status: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/admin/bulk-moderate`, { reviewIds, status });
  }
}

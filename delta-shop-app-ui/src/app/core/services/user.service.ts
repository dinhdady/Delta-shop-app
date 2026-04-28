import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UserAddress {
  id: string;
  type: string;
  recipientName: string;
  phone: string;
  email?: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
  postalCode?: string;
  isDefault: boolean;
  fullAddress: string;
}

export interface AddAddressRequest {
  type: string;
  recipientName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
  postalCode?: string;
  isDefault: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient) {}

  getAddresses(): Observable<UserAddress[]> {
    return this.http.get<UserAddress[]>(`${this.apiUrl}/me/addresses`);
  }

  getDefaultAddress(): Observable<UserAddress> {
    return this.http.get<UserAddress>(`${this.apiUrl}/me/addresses/default`);
  }

  addAddress(address: AddAddressRequest): Observable<UserAddress> {
    return this.http.post<UserAddress>(`${this.apiUrl}/me/addresses`, address);
  }

  setDefaultAddress(addressId: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/me/addresses/${addressId}/default`, {});
  }
}

import { Routes } from '@angular/router';
import { ProfileComponent } from './profile.component';
import { AddressesComponent } from './addresses/addresses.component';
import { ChangePasswordComponent } from './change-password/change-password.component';

export const PROFILE_ROUTES: Routes = [
  { path: '', component: ProfileComponent },
  { path: 'addresses', component: AddressesComponent },
  { path: 'change-password', component: ChangePasswordComponent }
];

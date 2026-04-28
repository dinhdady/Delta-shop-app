import { Routes } from '@angular/router';
import { MainLayoutComponent } from '../../shared/layouts/main-layout/main-layout.component';

export const SHOP_ROUTES: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', loadChildren: () => import('./home/home.routes').then(m => m.HOME_ROUTES) },
      { path: 'products', loadChildren: () => import('./products/products.routes').then(m => m.PRODUCTS_ROUTES) },
      { path: 'cart', loadChildren: () => import('./cart/cart.routes').then(m => m.CART_ROUTES) },
      { path: 'checkout', loadChildren: () => import('./checkout/checkout.routes').then(m => m.CHECKOUT_ROUTES) },
      { path: 'profile', loadChildren: () => import('../profile/profile.routes').then(m => m.PROFILE_ROUTES) }
    ]
  }
];

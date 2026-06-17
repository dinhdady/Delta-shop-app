import { Routes } from '@angular/router';
import { AdminLayoutComponent } from '../../shared/layouts/admin-layout/admin-layout.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', loadChildren: () => import('./dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES) },
      { path: 'products', loadChildren: () => import('./products/products.routes').then(m => m.PRODUCTS_ROUTES) },
      { path: 'categories', loadChildren: () => import('./categories/categories.routes').then(m => m.CATEGORIES_ROUTES) },
      { path: 'brands', loadChildren: () => import('./brands/brands.routes').then(m => m.BRANDS_ROUTES) },
      { path: 'promotions', loadChildren: () => import('./promotions/promotions.routes').then(m => m.PROMOTIONS_ROUTES) },
      { path: 'orders', loadChildren: () => import('./orders/orders.routes').then(m => m.ORDERS_ROUTES) },
      { path: 'users', loadChildren: () => import('./users/users.routes').then(m => m.USERS_ROUTES) },
      {
        path: 'contacts',
        loadComponent: () => import('./contacts/admin-contact.component').then(m => m.AdminContactComponent)
      },
      {
        path: 'reviews',
        loadComponent: () => import('./reviews/admin-reviews.component').then(m => m.AdminReviewsComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];

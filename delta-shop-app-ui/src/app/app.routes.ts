import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { ForgotPasswordComponent } from './features/auth/forgot-password/forgot-password.component';
import { AboutComponent } from './features/about/about.component';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent),
    title: 'Delta Sports - Dụng Cụ Thể Thao Chuyên Nghiệp'
  },
  {
    path: 'products',
    loadComponent: () => import('./features/products/products.component').then(m => m.ProductsComponent),
    title: 'Sản Phẩm - Delta Sports'
  },
  {
      path: 'products/:id',
      loadComponent: () => import('./features/product-detail/product-detail.component').then(m => m.ProductDetailComponent)
    },
  {
      path: 'cart',
      loadComponent: () => import('./features/cart/cart.component').then(m => m.CartComponent)
    },
  {
    path: 'checkout',
    canActivate: [authGuard],
    loadComponent: () => import('./features/checkout/checkout.component').then(m => m.CheckoutComponent),
    title: 'Thanh Toán - Delta Sports'
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () => import('./features/orders/orders.component').then(m => m.OrdersComponent),
    title: 'Đơn hàng của tôi - Delta Sports'
  },
  {
    path: 'wishlist',
    canActivate: [authGuard],
    loadComponent: () => import('./features/wishlist/wishlist.component').then(m => m.WishlistComponent),
    title: 'Yêu thích - Delta Sports'
  },

  {
    path: 'payment-result',
    loadComponent: () => import('./features/payment-result/payment-result.component').then(m => m.PaymentResultComponent),
    title: 'Kết quả thanh toán - Delta Sports'
  },
  {
    path: 'login',
    component: LoginComponent,
    title: 'Đăng Nhập - Delta Sports'
  },
  {
    path: 'register',
    component: RegisterComponent,
    title: 'Đăng Ký - Delta Sports'
  },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
    title: 'Quên Mật Khẩu - Delta Sports'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  {
  path: 'contact',
    loadComponent: () => import('./features/contact/contact.component').then(m => m.ContactComponent),
    title: 'Liên Hệ - Delta Sports'
  },
  {
  path: 'categories',
  loadComponent: () => import('./features/categories/categories.component').then(m => m.CategoriesComponent),
  title: 'Danh Mục Sản Phẩm - Delta Sports'
},
{
  path: 'categories/:slug',
  loadComponent: () => import('./features/categories/category-detail.component').then(m => m.CategoryDetailComponent),
  title: 'Danh Mục - Delta Sports'
},
{ path: 'about', component: AboutComponent },
{path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent), canActivate: [authGuard], title: 'Tài Khoản - Delta Sports'},
  // Fallback
  {
    path: '**',
    redirectTo: ''
  }
];

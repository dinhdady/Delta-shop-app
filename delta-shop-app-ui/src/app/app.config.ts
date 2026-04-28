import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http'; // Bỏ withInterceptorsFromDi
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import {
  LucideAngularModule,
  Search,
  ShoppingCart,
  User,
  Facebook,
  Instagram,
  Twitter,
  MapPin,
  Phone,
  Mail,
  Filter,
  Check,
  Heart,
  Shield,
  Truck,
  RefreshCw,
  Trash2,
  ArrowRight,
  Eye,
  AlertCircle,
  CheckCircle,
  EyeOff,
  ArrowLeft
} from 'lucide-angular';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(
      routes,
      withComponentInputBinding(),
      withInMemoryScrolling({ scrollPositionRestoration: 'top' })
    ),
    provideHttpClient(
      withInterceptors([authInterceptor]) // CHỈ dùng functional interceptor
    ),
    provideAnimations(),
    provideToastr({
      positionClass: 'toast-bottom-right',
      preventDuplicates: true,
      timeOut: 3000,
      progressBar: true
    }),
    importProvidersFrom(
      LucideAngularModule.pick({
        Search,
        ShoppingCart,
        User,
        Facebook,
        Instagram,
        Twitter,
        MapPin,
        Phone,
        Mail,
        Filter,
        Check,
        Heart,
        Shield,
        Truck,
        RefreshCw,
        Trash2,
        ArrowRight,
        Eye,
        AlertCircle,
        CheckCircle,
        EyeOff,
        ArrowLeft
      })
    )
  ]
};

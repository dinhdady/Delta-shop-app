import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent],
  template: `
    <app-navbar></app-navbar>
    <main class="main-container">
      <router-outlet></router-outlet>
    </main>
    @if (!isAdminRoute()) {
      <app-footer></app-footer>
    }
  `,
  styles: [`
    .main-container {
      min-height: calc(100vh - 80px - 400px); /* Adjust based on exact navbar/footer height */
    }
  `]
})
export class AppComponent {
  private router = inject(Router);
  title = 'Delta Sports';

  isAdminRoute(): boolean {
    return this.router.url.startsWith('/admin');
  }
}

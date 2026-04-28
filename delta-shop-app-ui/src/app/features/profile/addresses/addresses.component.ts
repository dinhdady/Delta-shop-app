import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-addresses',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="addresses-container">
      <h2>My Addresses</h2>
    </div>
  `
})
export class AddressesComponent {}

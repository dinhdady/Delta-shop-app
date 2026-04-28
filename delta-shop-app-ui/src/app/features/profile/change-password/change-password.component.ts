import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="change-password-container">
      <h2>Change Password</h2>
    </div>
  `
})
export class ChangePasswordComponent {}

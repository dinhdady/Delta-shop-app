import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="home">
      <h1>Chào mừng đến với Delta Sports</h1>
      <p>Trang chủ đang được xây dựng</p>
      <button routerLink="/products">Xem sản phẩm</button>
    </div>
  `
})
export class HomeComponent {}

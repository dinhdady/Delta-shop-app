import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton-loader',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (type === 'card') {
      <div class="skeleton-card">
        <div class="skeleton-image"></div>
        <div class="skeleton-content">
          <div class="skeleton-text category"></div>
          <div class="skeleton-text title"></div>
          <div class="skeleton-text title-short"></div>
          <div class="skeleton-text price"></div>
        </div>
      </div>
    }
    
    @if (type === 'detail') {
      <div class="skeleton-detail">
        <div class="skeleton-image-large"></div>
        <div class="skeleton-info">
          <div class="skeleton-text title-large"></div>
          <div class="skeleton-text price-large"></div>
          <div class="skeleton-text desc-line"></div>
          <div class="skeleton-text desc-line"></div>
          <div class="skeleton-text desc-line short"></div>
          <div class="skeleton-box"></div>
          <div class="skeleton-button"></div>
        </div>
      </div>
    }
  `,
  styles: [`
    @keyframes shimmer {
      0% {
        background-position: -468px 0;
      }
      100% {
        background-position: 468px 0;
      }
    }

    .skeleton-image, .skeleton-text, .skeleton-image-large, .skeleton-box, .skeleton-button {
      background: #f6f7f8;
      background-image: linear-gradient(
        to right,
        #f6f7f8 0%,
        #edeef1 20%,
        #f6f7f8 40%,
        #f6f7f8 100%
      );
      background-repeat: no-repeat;
      background-size: 800px 104px; 
      display: inline-block;
      position: relative; 
      animation: shimmer 1.5s infinite linear forwards;
    }

    /* Card Skeleton */
    .skeleton-card {
      border: 1px solid var(--color-border);
      border-radius: var(--radius);
      overflow: hidden;
    }

    .skeleton-image {
      width: 100%;
      aspect-ratio: 1;
    }

    .skeleton-content {
      padding: 1.25rem;
    }

    .skeleton-text {
      height: 12px;
      margin-bottom: 0.5rem;
      border-radius: 2px;
      
      &.category { width: 40%; margin-bottom: 0.75rem; }
      &.title { width: 90%; }
      &.title-short { width: 60%; margin-bottom: 1rem; }
      &.price { width: 30%; height: 18px; margin-bottom: 0; }
    }

    /* Detail Skeleton */
    .skeleton-detail {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 3rem;
      
      @media (max-width: 768px) {
        grid-template-columns: 1fr;
      }
    }

    .skeleton-image-large {
      width: 100%;
      aspect-ratio: 1;
      border-radius: var(--radius);
    }

    .skeleton-info {
      padding-top: 2rem;
    }

    .title-large { width: 80%; height: 32px; margin-bottom: 1rem; }
    .price-large { width: 30%; height: 24px; margin-bottom: 2rem; }
    .desc-line { width: 100%; margin-bottom: 0.5rem; }
    .short { width: 70%; margin-bottom: 2rem; }
    .skeleton-box { width: 100%; height: 60px; margin-bottom: 2rem; border-radius: var(--radius); }
    .skeleton-button { width: 200px; height: 48px; border-radius: var(--radius); }
  `]
})
export class SkeletonLoaderComponent {
  @Input() type: 'card' | 'detail' = 'card';
}

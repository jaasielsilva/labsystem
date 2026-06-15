import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="placeholder-page animate-fade-in">
      <div class="glass-card placeholder-card">
        <span class="placeholder-icon">{{ icon }}</span>
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-subtitle">{{ description }}</p>
        <a routerLink="/clientes" class="btn btn-secondary">Voltar ao início</a>
      </div>
    </div>
  `,
  styles: [`
    .placeholder-page {
      display: flex;
      justify-content: center;
      align-items: flex-start;
      padding: 48px 24px;
    }

    .placeholder-card {
      max-width: 480px;
      width: 100%;
      text-align: center;
      padding: 48px 32px;
    }

    .placeholder-icon {
      font-size: 3rem;
      display: block;
      margin-bottom: 16px;
    }

    .page-title {
      font-size: 1.75rem;
      margin-bottom: 8px;
    }

    .page-subtitle {
      margin-bottom: 28px;
    }
  `]
})
export class PlaceholderComponent {
  private route = inject(ActivatedRoute);

  title = this.route.snapshot.data['title'] ?? 'Em desenvolvimento';
  description = this.route.snapshot.data['description'] ?? 'Este módulo será implementado em breve.';
  icon = this.route.snapshot.data['icon'] ?? '🚧';
}

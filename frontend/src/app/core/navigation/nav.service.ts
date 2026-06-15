import { Injectable, computed, inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { NAV_SECTIONS, NavItem, NavSection } from './nav.config';

@Injectable({ providedIn: 'root' })
export class NavService {
  private auth = inject(AuthService);

  readonly sections = computed(() => this.buildVisibleSections());

  canAccess(item: NavItem): boolean {
    return this.auth.hasRole(...item.roles);
  }

  private buildVisibleSections(): NavSection[] {
    this.auth.usuarioAtual();

    return NAV_SECTIONS.map((section) => ({
      ...section,
      items: section.items.filter((item) => this.canAccess(item))
    })).filter((section) => section.items.length > 0);
  }
}

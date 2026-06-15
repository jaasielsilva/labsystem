import { Injectable, signal } from '@angular/core';

/**
 * Contexto da empresa (tenant). Placeholder até multi-tenant no JWT.
 */
@Injectable({ providedIn: 'root' })
export class TenantContextService {
  readonly empresaId = signal<number | null>(null);
  readonly empresaNome = signal('Laboratório Demo');
}

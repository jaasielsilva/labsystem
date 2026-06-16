import { Injectable, signal } from '@angular/core';
import { Usuario } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
  readonly empresaId = signal<number | null>(null);
  readonly empresaNome = signal('—');
  readonly isImpersonating = signal(false);

  syncFromUsuario(usuario: Usuario | null): void {
    if (usuario?.scope === 'TENANT_IMPERSONATION' && usuario.actingEmpresaId) {
      this.isImpersonating.set(true);
      this.empresaId.set(usuario.actingEmpresaId);
      this.empresaNome.set(usuario.actingEmpresaNome ?? '—');
      return;
    }

    this.isImpersonating.set(false);
    this.empresaId.set(usuario?.empresaId ?? null);
    this.empresaNome.set(usuario?.empresaNome ?? '—');
  }

  clear(): void {
    this.empresaId.set(null);
    this.empresaNome.set('—');
    this.isImpersonating.set(false);
  }
}

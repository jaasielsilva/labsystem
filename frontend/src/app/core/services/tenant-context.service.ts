import { Injectable, signal } from '@angular/core';
import { Usuario } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
  readonly empresaId = signal<number | null>(null);
  readonly empresaNome = signal('—');

  syncFromUsuario(usuario: Usuario | null): void {
    this.empresaId.set(usuario?.empresaId ?? null);
    this.empresaNome.set(usuario?.empresaNome ?? '—');
  }

  clear(): void {
    this.empresaId.set(null);
    this.empresaNome.set('—');
  }
}

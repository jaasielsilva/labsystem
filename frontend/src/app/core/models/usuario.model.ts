export type Perfil = 'SUPER_ADMIN' | 'ADMIN' | 'OPERADOR' | 'VISUALIZADOR';
export type AccessScope = 'PLATFORM' | 'TENANT' | 'TENANT_IMPERSONATION';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  empresaId: number;
  empresaNome: string;
  scope?: AccessScope;
  actingEmpresaId?: number | null;
  actingEmpresaNome?: string | null;
}

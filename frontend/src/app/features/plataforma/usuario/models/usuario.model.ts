export type Perfil = 'SUPER_ADMIN' | 'ADMIN' | 'OPERADOR' | 'VISUALIZADOR';

export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  senha?: string;
  ativo: boolean;
  perfil: Exclude<Perfil, 'SUPER_ADMIN'>;
  empresaId?: number;
  empresaNome?: string;
}

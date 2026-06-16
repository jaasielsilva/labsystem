export type Perfil = 'ADMIN' | 'OPERADOR' | 'VISUALIZADOR';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  empresaId: number;
  empresaNome: string;
}

export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  senha?: string;
  ativo: boolean;
  perfil: 'ADMIN' | 'OPERADOR' | 'VISUALIZADOR';
}
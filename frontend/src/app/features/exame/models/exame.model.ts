export interface Exame {
  id?: number;
  codigo: string;
  nome: string;
  descricao?: string;
  categoria?: {
    id: number;
    nome: string;
  };
  tipoAmostra: 'SANGUE' | 'URINA' | 'FEZES' | 'OUTRO';
  prazoDias: number;
  valor?: number;
  ativo: boolean;
  createdAt?: string;
  updatedAt?: string;
}
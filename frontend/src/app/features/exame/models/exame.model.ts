export type TipoAmostra = 'SANGUE' | 'URINA' | 'FEZES' | 'OUTRO';

export interface Exame {
  id?: number;
  codigo: string;
  nome: string;
  descricao?: string;
  categoria?: string;
  tipoAmostra: TipoAmostra;
  prazoDias: number;
  valor?: number;
  ativo: boolean;
  createdAt?: string;
  updatedAt?: string;
}

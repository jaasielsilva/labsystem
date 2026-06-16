export interface Empresa {
  id?: number;
  nome: string;
  cnpj: string;
  email: string;
  telefone?: string;
  endereco?: string;
  cidade?: string;
  uf?: string;
  ativo: boolean;
  createdAt?: string;
  updatedAt?: string;
}

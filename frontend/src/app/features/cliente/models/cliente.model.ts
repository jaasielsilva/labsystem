export interface Cliente {
  id?: number;
  nome: string;
  cpf: string;
  email: string;
  telefone?: string;
  dataNascimento: string;
  ativo: boolean;
  createdAt?: string;
  updatedAt?: string;
}

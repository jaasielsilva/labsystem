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

export interface PrimeiroAdmin {
  nome: string;
  email: string;
  senha: string;
}

export interface LaboratorioOnboarding {
  laboratorio: Omit<Empresa, 'id' | 'createdAt' | 'updatedAt'>;
  admin: PrimeiroAdmin;
}

export interface LaboratorioOnboardingResult {
  laboratorio: Empresa;
  admin: {
    id: number;
    nome: string;
    email: string;
    perfil: string;
    empresaId: number;
    empresaNome?: string;
    ativo: boolean;
  };
}

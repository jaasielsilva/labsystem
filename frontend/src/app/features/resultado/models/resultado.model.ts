export type ResultadoStatus = 'PENDENTE' | 'EM_ANALISE' | 'DISPONIVEL' | 'CANCELADO';
export type PedidoStatus = 'ABERTO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';

export interface Resultado {
  id: number;
  pedidoId: number;
  pedidoItemId: number;
  clienteId: number;
  clienteNome: string;
  pedidoStatus: PedidoStatus;
  exameId: number;
  exameCodigo: string;
  exameNome: string;
  status: ResultadoStatus;
  laudo?: string;
  observacaoInterna?: string;
  dataLiberacao?: string;
  dataPedido?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ResultadoSummary {
  id: number;
  pedidoId: number;
  clienteNome: string;
  exameCodigo: string;
  exameNome: string;
  status: ResultadoStatus;
  pedidoStatus: PedidoStatus;
  dataPedido: string;
  dataLiberacao?: string;
}

export interface ResultadoUpdatePayload {
  observacaoInterna?: string;
  laudo?: string;
}

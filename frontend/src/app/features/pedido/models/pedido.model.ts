export type PedidoStatus = 'ABERTO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';

export interface PedidoItem {
  id?: number;
  exameId: number;
  exameCodigo?: string;
  exameNome?: string;
  valorUnitario?: number;
}

export interface Pedido {
  id?: number;
  clienteId: number;
  clienteNome?: string;
  status?: PedidoStatus;
  observacao?: string;
  motivoCancelamento?: string;
  dataPedido?: string;
  itens: PedidoItem[];
  valorTotal?: number;
  quantidadeItens?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PedidoSummary {
  id: number;
  clienteId: number;
  clienteNome: string;
  status: PedidoStatus;
  dataPedido: string;
  quantidadeItens: number;
  valorTotal: number;
}

export interface PedidoPayload {
  clienteId: number;
  observacao?: string;
  itens: { exameId: number }[];
}

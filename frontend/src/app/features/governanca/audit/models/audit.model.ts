export interface AuditLog {
  id: number;
  usuarioEmail: string;
  perfil: string;

  action: string;
  entidade: string;
  entidadeId: number;

  detalhes: string;

  scope: string;

  createdAt: string;
}

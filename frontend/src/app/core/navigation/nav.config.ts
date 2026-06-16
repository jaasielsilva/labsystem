import { Perfil } from '../models/usuario.model';

export interface NavItem {
  id: string;
  label: string;
  route: string;
  icon: string;
  roles: Perfil[];
  disabled?: boolean;
  disabledLabel?: string;
}

export interface NavSection {
  id: string;
  label: string;
  items: NavItem[];
}

export const NAV_SECTIONS: NavSection[] = [
  {
    id: 'operacional',
    label: 'Operacional',
    items: [
      {
        id: 'clientes',
        label: 'Clientes',
        route: '/clientes',
        icon: '👥',
        roles: ['ADMIN', 'OPERADOR', 'VISUALIZADOR']
      },
      {
        id: 'pedidos',
        label: 'Pedidos',
        route: '/pedidos',
        icon: '📋',
        roles: ['ADMIN', 'OPERADOR', 'VISUALIZADOR'],
        disabled: true,
        disabledLabel: 'Em breve'
      },
      {
        id: 'exames',
        label: 'Exames',
        route: '/exames',
        icon: '🧪',
        roles: ['ADMIN', 'OPERADOR', 'VISUALIZADOR']
      },
      {
        id: 'resultados',
        label: 'Resultados',
        route: '/resultados',
        icon: '📄',
        roles: ['ADMIN', 'OPERADOR', 'VISUALIZADOR'],
        disabled: true,
        disabledLabel: 'Em breve'
      }
    ]
  },
  {
    id: 'relatorios',
    label: 'Relatórios',
    items: [
      {
        id: 'relatorios',
        label: 'Relatórios',
        route: '/relatorios',
        icon: '📊',
        roles: ['ADMIN', 'OPERADOR', 'VISUALIZADOR'],
        disabled: true,
        disabledLabel: 'Em breve'
      }
    ]
  },
  {
    id: 'governanca',
    label: 'Governança',
    items: [
      {
        id: 'usuarios',
        label: 'Usuários',
        route: '/governanca/usuarios',
        icon: '🔐',
        roles: ['ADMIN']
      },
      {
        id: 'permissoes',
        label: 'Perfis e permissões',
        route: '/governanca/permissoes',
        icon: '🛡️',
        roles: ['ADMIN']
      },
      {
        id: 'empresa',
        label: 'Empresa e plano',
        route: '/governanca/empresas',
        icon: '🏢',
        roles: ['ADMIN']
      },
      {
        id: 'auditoria',
        label: 'Auditoria',
        route: '/governanca/auditoria',
        icon: '📝',
        roles: ['ADMIN']
      }
    ]
  },
  {
    id: 'dev',
    label: 'Desenvolvimento',
    items: [
      {
        id: 'design-system',
        label: 'Design System',
        route: '/dev/ui',
        icon: '🎨',
        roles: ['ADMIN']
      }
    ]
  }
];

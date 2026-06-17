---
name: labsystem
description: Padrão empresarial único do Labsystem. Use em QUALQUER desenvolvimento — nova feature, correção, refactor, deploy ou revisão. Garante produto vendável, código consistente e sistema pronto para produção ao longo do tempo.
---

# Labsystem — Padrão Empresarial

Skill mestre do projeto. Siga **sempre** que for desenvolver, revisar ou planejar algo no Labsystem.

Documento técnico complementar (estrutura de pastas, stack, convenções): `docs/ARQUITETURA.md`.

---

## 1. Princípio central

Construir um **produto de laboratório vendável**, não só CRUDs corretos.

Toda entrega deve cobrir seis pilares:

| Pilar | Pergunta-chave |
|-------|----------------|
| **Produto** | Qual dor real isso resolve? |
| **Arquitetura** | Segue o padrão do projeto? |
| **UX** | O usuário consegue usar sem ajuda? |
| **Segurança** | Quem pode fazer o quê? |
| **Deploy** | Sobe em produção sem surpresa? |
| **Observabilidade** | Dá para debugar em prod? |

Se um pilar ficar de fora, a feature **não está pronta**.

> **Docs = alvo + estado real.** Seções abaixo com 🔲 ainda não existem no código — não assumir que estão prontas.

---

## 2. Estado atual do projeto

Snapshot sincronizado com o repositório. Atualizar ao fechar cada entrega.

### O que já existe

| Área | Implementado |
|------|----------------|
| Backend | `src/` na raiz, Spring Boot 3.2, Flyway (V1–V9), `ApiResponse`, exceptions |
| **Auth JWT** | `features/auth/`, login/refresh/me, BCrypt, filtro JWT, `@PreAuthorize` |
| Perfis | `SUPER_ADMIN`, `ADMIN`, `OPERADOR`, `VISUALIZADOR` — seed dev no `DataSeeder` |
| **Multi-tenant por empresa** | `empresa_id` em todas as tabelas de negócio; `TenantContext`; isolamento real por laboratório |
| **JWT tenant** | Claims `empresaId`, `scope` (`PLATFORM`, `TENANT`, `TENANT_IMPERSONATION`) |
| **Acesso tenant** | `TenantAccessEvaluator` (back) + `hasTenantAccess()` (front, inclui impersonação) |
| **SUPER_ADMIN (plataforma)** | `/api/v1/platform/**`, front `/plataforma/*` — laboratórios e usuários globais |
| **Onboarding laboratório** | `POST /platform/empresas/onboarding` — lab + primeiro ADMIN em um passo |
| **Impersonação (suporte)** | SUPER_ADMIN entra no laboratório; banner “Modo suporte”; APIs operacionais liberadas |
| Módulo **Cliente** | CRUD tenant-aware, RBAC, testes |
| Módulo **Exame** (catálogo) | CRUD tenant-aware em `/exames`, testes |
| Módulo **Pedido** | Pedido + `pedido_itens` (múltiplos exames), status, FK cliente |
| Módulo **Resultado** | Laudo por item do pedido; sync de status com pedido (`EM_ANDAMENTO` → `CONCLUIDO`) |
| Governança tenant | Usuários do laboratório em `/governanca/usuarios` (ADMIN) |
| Frontend `core/` | `auth.service`, guards (`tenantGuard`, `superAdminGuard`), interceptors |
| **Design System LS1 Light** | `styles.css`; playground `/dev/ui` |
| **App Shell** | Sidebar + topbar; menu em `nav.config.ts` |
| **Toast LS1** | `ToastService` global |
| **TenantContextService** | `empresaId` / `empresaNome` (ou laboratório impersonado) via `/auth/me` |

**Usuários dev (senha no `DataSeeder`):**

| E-mail | Senha | Perfil |
|--------|-------|--------|
| super@labsystem.local | super123 | SUPER_ADMIN |
| admin@labsystem.local | admin123 | ADMIN |
| operador@labsystem.local | operador123 | OPERADOR |
| visualizador@labsystem.local | visualizador123 | VISUALIZADOR |

### O que ainda não existe (não inventar no código)

| Área | Pendente |
|------|----------|
| **Planos + limites + billing** (Fase 4 — comercial) | 🔲 |
| Relatórios e exportação | 🔲 |
| Entrega/cobrança ao paciente | 🔲 |
| Docker, compose, `.env.example`, `application-prod.yml` | 🔲 |
| traceId/MDC, logs prod refinados | 🔲 |
| OpenAPI / Swagger | 🔲 |

### Referência viva

- CRUD: `features/cliente/` (back e front)
- Auth: `features/auth/` + `frontend/src/app/core/`

### Design System — LS1 Light (aprovado)

Tema **claro hospitalar / laboratório**: fundo branco e azul clínico, cards brancos, teal de saúde como primária, azul para links e destaques.

| Token | Valor | Uso |
|-------|-------|-----|
| `--color-primary` | `#0d9488` | Botões principais, foco, marca |
| `--color-accent` | `#0284c7` | Links, e-mails, info clínica |
| `--bg-page` | `#eef6f9` | Fundo da aplicação |
| `--card-bg` | `#ffffff` | Cards e painéis |
| `--text-primary` | `#0f172a` | Texto principal |
| `--text-secondary` | `#64748b` | Labels, hints |

- **Fonte:** Plus Jakarta Sans
- **Primitivos globais:** `frontend/src/styles.css` — usar classes `.glass-card`, `.btn-*`, `.form-*`, `.premium-table`, `.alert-*`, `.badge-*`
- **Playground:** `/dev/ui` (somente **ADMIN**)
- **Regra:** novas telas usam variáveis CSS globais; evitar cores hardcoded escuras ou tema dark

### Gaps conhecidos (antes do DoD empresarial completo)

- `GlobalExceptionHandler` ainda expõe mensagem interna em erros 500 genéricos
- Sem Docker para deploy
- CORS ainda fixo em dev (`localhost:4200`)
- Sem enforcement de limites por plano (Fase 4)

### Próxima entrega recomendada

**Docker/prod** ou **relatórios** — conforme priorização da seção 3. **Planos + billing** só após estabilizar deploy.

---

## 3. Domínio e priorização

Fluxo de negócio do Labsystem:

```
Cliente → Pedido → Exame(s) → Resultado → Entrega/Cobrança
```

Ordem sugerida de evolução:

1. ~~Autenticação e perfis~~ ✅
2. ~~**Fundação tenant** — `empresa_id`, JWT, isolamento por laboratório~~ ✅
3. ~~Cadastros base (clientes, exames)~~ ✅
4. ~~Plataforma SUPER_ADMIN (laboratórios, onboarding, impersonação)~~ ✅
5. ~~Fluxo principal (pedido → resultado)~~ ✅
6. Relatórios e exportação
7. Deploy (Docker/prod)
8. **Comercial** — planos, limites, billing (seção 6.1 — Fase 4)
9. Integrações externas

Feature fora desse fluxo exige justificativa explícita.

### Antes de codar (obrigatório)

Se o pedido for vago, **pergunte** antes de implementar:

1. Quem usa? (persona)
2. Qual dor resolve?
3. Como medir sucesso?
4. O que é MVP vs v2?

---

## 4. Fluxo de nova feature

Ordem fixa — não pular etapas:

```
1. Entity (JPA)
2. Migration Flyway
3. Repository
4. DTOs (Request + Response)
5. Service (interface + impl)
6. Controller REST
7. Testes unitários do Service
8. Model Angular
9. Service Angular
10. Pages + Form (standalone)
11. Rota lazy
```

### Backend (`com.jaasielsilva.labsystem`)

```
src/main/java/.../features/{feature}/
├── entity/ repository/ dto/ mapper/ service/impl/ controller/
```

- Nunca expor Entity na API
- Validação em Request DTO (`jakarta.validation`)
- Resposta sempre `ApiResponse<T>`
- SLF4J — nunca `System.out.println`
- Migration: `V{n}__descricao.sql` — tabelas `snake_case` com `id`, `created_at`, `updated_at`
- Tabelas de **negócio** (clientes, exames, pedidos…): coluna `empresa_id` FK → `empresas` (seção 6.1)
- Services de negócio: **sempre** filtrar pelo `empresaId` do JWT — nunca confiar no body

### API REST

```
GET    /api/v1/{recurso}         → listar (paginado)
GET    /api/v1/{recurso}/{id}    → buscar
POST   /api/v1/{recurso}         → criar
PUT    /api/v1/{recurso}/{id}    → atualizar
DELETE /api/v1/{recurso}/{id}    → deletar
```

### Frontend

```
frontend/src/app/features/{feature}/
├── models/ services/ pages/ {feature}.routes.ts
```

- Standalone Components, Reactive Forms, lazy loading
- HttpClient só em services; API via `environment.ts`
- Referência viva: `features/cliente/`

### App Shell (layout modular)

Rotas autenticadas são **filhas** de `AppShellComponent` (`core/layout/app-shell/`).

```
core/
├── layout/app-shell/     → sidebar + topbar + router-outlet
├── navigation/
│   ├── nav.config.ts     → seções e itens do menu (roles, disabled)
│   └── nav.service.ts    → filtra menu por perfil
└── services/tenant-context.service.ts  → empresa ativa na UI (Fase 1: ler de /auth/me)
```

- **Novo módulo:** adicionar item em `nav.config.ts` + rota lazy em `app.routes.ts` (children do shell)
- **Plataforma:** seção `plataforma` — só `SUPER_ADMIN`; laboratórios em `/plataforma/laboratorios`
- **Governança tenant:** seção `governanca` — só `ADMIN`; usuários em `/governanca/usuarios`
- **Itens `disabled: true`:** visíveis no menu com badge "Em breve" (roadmap)
- **Topbar:** empresa + usuário + perfil; **sidebar:** navegação por módulo
- Login (`/login`) fica **fora** do shell

> **Plataforma (cadastro de laboratórios)** ≠ **isolamento multi-tenant**. O cadastro é escopo SUPER_ADMIN; o isolamento operacional é sempre por `empresa_id` do JWT (seção 6).

---

## 5. UX mínima (toda tela)

| Estado | Comportamento |
|--------|---------------|
| Loading | Spinner; desabilitar ações duplicadas |
| Empty | Mensagem + CTA ("Cadastrar primeiro") |
| Error | `ToastService.error()` — mensagem clara; sem stack trace |
| Success | `ToastService.success()` — após salvar/excluir/ação concluída |

### Toast (padrão obrigatório para feedback transitório)

Feedback de **ação** (salvar, excluir, erro de API, login inválido) usa **toast flutuante**, não alert inline na página.

```
core/
├── services/toast.service.ts      → success(), error(), warning(), info()
└── components/toast-container/  → montado em app.component (global)
```

**Uso em qualquer feature:**

```typescript
private toast = inject(ToastService);

this.toast.success('Cliente cadastrado com sucesso.');
this.toast.error(err.error?.message || 'Erro ao salvar.');
this.toast.warning('Limite do plano quase atingido.');
this.toast.info('Novo resultado disponível.');
```

| Tipo | Duração padrão | Quando usar |
|------|----------------|-------------|
| `success` | 4s | CRUD ok, exclusão, ação concluída |
| `error` | 6s | Falha de API, timeout, permissão |
| `warning` | 5s | Avisos de negócio, limites |
| `info` | 4s | Informações não críticas |

- Máximo **5 toasts** visíveis; fechar com **×** ou auto-dismiss
- Cores alinhadas ao LS1 Light (mesma paleta dos `.alert-*`)
- **Não** duplicar: se usou toast, não coloque `alert` inline para o mesmo evento
- **Alerts inline** (`.alert-*`) só para aviso **fixo no contexto da tela** (ex.: banner de manutenção)
- Validação de campo continua em `.invalid-feedback` no formulário

Formulários: label + `*`, erro após `touched`, botão desabilitado se `invalid` ou salvando.

Listagens: **busca no toolbar** (`q` no backend, debounce ~350ms), paginação com rodapé fixo, confirmação antes de excluir, formatação (telefone, data). **CPF mascarado no grid** (`123.***.***-01`) — LGPD; CPF completo só em formulário autorizado.

Textos em **português claro** — nunca "Erro 500" ou jargão técnico na UI.

### Visual (LS1 Light)

- Tema claro obrigatório — ambiente hospitalar/laboratorial
- Cards brancos com borda `#e2e8f0` e sombra suave (classe `.glass-card`)
- Primária teal `#0d9488`; accent azul `#0284c7`; perigo `#dc2626`
- **Toast** para ações (padrão); alerts inline `.alert-*` só para contexto fixo na página
- Status em listagens: `.badge-green`, `.badge-blue`, `.badge-yellow`, `.badge-gray`, `.badge-rose`
- Referência visual: `/dev/ui` e `frontend/src/styles.css`
- Layout autenticado: App Shell com sidebar (LS1 Light) — não criar telas full-page sem o shell

---

## 6. Usuários, perfis, empresas e tenant

Obrigatório para produto multi-usuário ou SaaS.

**Perfis iniciais:** `SUPER_ADMIN`, `ADMIN`, `OPERADOR`, `VISUALIZADOR`.

- `SUPER_ADMIN` — operador da plataforma; escopo `/api/v1/platform/**`; acesso operacional **somente via impersonação**
- `ADMIN` — gestor do laboratório (tenant); escopo filtrado por `empresa_id` do JWT
- `OPERADOR` / `VISUALIZADOR` — tenant; leitura/escrita conforme RBAC

- Permissão validada no **backend** (`@tenantAccess`, `@platformAccess`, `@PreAuthorize`)
- Frontend esconde ação; backend retorna 403 se sem permissão
- JWT: login, refresh, `/auth/me`
- Senha BCrypt; nunca logar nem retornar senha/token

### 6.1 Evolução multi-tenant e comercial (padrão oficial)

O Labsystem **já opera multi-tenant por laboratório** (`empresa_id`). Isso é **independente** de planos e billing — que entram na **Fase 4**.

```
┌─────────────────────────────────────────────────────────────┐
│  HOJE — Isolamento por empresa (tenant = laboratório)       │
├─────────────────────────────────────────────────────────────┤
│  SUPER_ADMIN (plataforma)                                   │
│       │                                                     │
│       ├── Laboratório A  ── empresa_id=2  ── clientes,      │
│       │                                    pedidos, …       │
│       └── Laboratório B  ── empresa_id=3  ── dados isolados │
│                                                             │
│  Cada usuário tenant pertence a UMA empresa (JWT empresaId) │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  FUTURO — Camada comercial (Fase 4, ainda não implementada)  │
├─────────────────────────────────────────────────────────────┤
│  Laboratório A  →  Plano Pro  →  limites + fatura mensal    │
│  Laboratório B  →  Plano Starter → limites menores          │
└─────────────────────────────────────────────────────────────┘
```

| Fase | Nome | O que entrega | Status |
|------|------|---------------|--------|
| **1** | **Tenant** | `empresas`, `empresa_id`, JWT, `TenantContext`, filtro em todos os services | ✅ |
| **2** | **Plataforma** | SUPER_ADMIN, CRUD laboratórios, usuários globais, onboarding, impersonação | ✅ |
| **3** | **Fluxo operacional** | Pedidos (`pedido` + `pedido_itens`), Resultados (laudo por item) | ✅ |
| **4** | **Comercial** | Planos, limites por recurso, billing/assinatura | 🔲 |

#### Fase 1 — Tenant (implementado)

**Conceito:** `empresa_id` = identificador do tenant. No dia a dia, tenant = **laboratório** (`tipo = LABORATORIO`). Existe empresa sentinela `PLATAFORMA` para o SUPER_ADMIN.

**Tabelas com `empresa_id` (negócio):** `usuarios`, `clientes`, `exames`, `pedidos`, `resultados`.

**Tabelas filhas** (`pedido_itens`): isolamento via FK ao pedido pai (que tem `empresa_id`). Services **nunca** consultam sem filtro de tenant.

**Backend**

1. `TenantContext.requireTenantEmpresaId()` — JWT ou `actingEmpresaId` na impersonação
2. `TenantAccessEvaluator` — `@tenantAccess.read()`, `.write()`, `.admin()`
3. Repositories: `findByIdAndEmpresaId`, `findAllByEmpresaId`, etc.
4. **Nunca** aceitar `empresaId` do body para autorizar

**Frontend**

1. `TenantContextService` via `/auth/me`
2. `tenantGuard` — bloqueia SUPER_ADMIN fora da impersonação nas rotas operacionais
3. `hasTenantAccess()` — menu e guards alinhados à impersonação
4. Services HTTP **não** enviam `empresaId` no payload

#### Fase 2 — Plataforma (implementado)

- API `/api/v1/platform/**` com `PlatformAccessEvaluator`
- Front `/plataforma/laboratorios`, `/plataforma/usuarios`
- Onboarding: laboratório + primeiro ADMIN (`/platform/empresas/onboarding`)
- Impersonação: `scope=TENANT_IMPERSONATION`, banner “Modo suporte”

#### Fase 3 — Fluxo operacional (implementado)

- **Pedido:** múltiplos exames (`pedido_itens`), FK `cliente_id`, status `ABERTO` → `EM_ANDAMENTO` → `CONCLUIDO` / `CANCELADO`
- **Resultado:** 1:1 com `pedido_item`; auto-provisionado ao criar pedido; liberação sincroniza status do pedido

#### Fase 4 — Planos + billing (futuro — não implementar sem spec)

Escopo previsto (ainda **sem código**):

- Tabela `planos` (nome, limites: max usuários, clientes, pedidos/mês, flags de módulo)
- Vínculo `empresas.plano_id` ou tabela `assinaturas`
- `PlanoService.limiteAtingido(empresaId, recurso)` nos services de criação
- Billing externo (ex.: gateway de pagamento) — faturas, trial, upgrade/downgrade
- UI SUPER_ADMIN: gestão de planos; UI tenant: uso vs limite (toast `warning`)

```java
// Exemplo futuro — NÃO existe no código hoje
if (planoService.limiteAtingido(empresaId, "clientes")) {
    throw new BusinessException("Limite do plano atingido. Faça upgrade.");
}
```

**Regras invioláveis (todas as fases)**

- Coluna no banco: `empresa_id` (equivalente a `tenant_id`)
- Novo módulo de negócio → migration com `empresa_id NOT NULL`
- Isolamento por JWT, não por parâmetro do cliente
- Planos/limites **complementam** o tenant — não substituem o filtro por `empresa_id`

---

## 7. Logs e monitoramento

```java
private static final Logger log = LoggerFactory.getLogger(X.class);
```

| Nível | Quando |
|-------|--------|
| INFO | Operação de negócio relevante |
| WARN | Erro de negócio esperado (`BusinessException`) |
| ERROR | Falha inesperada (com stack trace) |

- Nunca logar senha, JWT completo ou CPF em claro em prod
- `traceId` via header `X-Trace-Id` + MDC em todo request
- Actuator: `/actuator/health` para Docker e load balancer
- `GlobalExceptionHandler`: 4xx sem stack; 500 genérico ao cliente em prod

---

## 8. Deploy e produção

- Build Docker multi-stage; config via `.env` (nunca commitar)
- Flyway na subida; `ddl-auto: validate` em prod
- Healthcheck em `db` e `backend`
- CORS restrito ao domínio do frontend
- Imagens versionadas (`labsystem-api:1.2.0`), não só `latest`
- Pipeline mínimo: test → build → deploy → smoke test em `/actuator/health`

---

## 9. Definition of Done — checklist único

Antes de considerar qualquer entrega concluída:

### Produto
- [ ] Resolve dor real; usuário completa o fluxo sem ajuda
- [ ] MVP definido; limitações declaradas

### Código
- [ ] Segue ordem do fluxo de feature (seção 4)
- [ ] Teste unitário do Service (happy path + erro de negócio)
- [ ] Migration testada; tabela em `ARQUITETURA.md` atualizada

### UX
- [ ] Loading, empty, error e success implementados
- [ ] Formulário com validação inline

### Segurança
- [ ] Endpoint protegido se dados sensíveis
- [ ] Sem secrets hardcoded
- [ ] Módulo de negócio: queries filtradas por `empresa_id` do JWT (seção 6.1)

### Operação
- [ ] Logs SLF4J nas operações importantes
- [ ] Pronto para subir com Docker (não quebra compose existente)

---

## 10. Anti-padrões (nunca fazer)

- CRUD sem jornada de uso
- Backend completo sem tela para o usuário final
- Permissão só no frontend
- `ddl-auto: create` em produção
- Alterar banco manualmente (sem Flyway)
- Feature "porque o concorrente tem"
- Campos no formulário que a operação não usa
- Query de negócio **sem** filtro por `empresa_id`
- Aceitar `empresaId` do body do cliente para decidir tenant
- CRUD de negócio em tabela nova **sem** coluna `empresa_id`

---

## 11. Evolução contínua

Ao fechar cada módulo:

1. Atualizar tabelas em `docs/ARQUITETURA.md` (módulos + infraestrutura)
2. Atualizar **seção 2 (Estado atual)** desta skill
3. Manter `features/cliente/` como referência até existir módulo mais completo no DoD

Invocar no Cursor: `/labsystem`

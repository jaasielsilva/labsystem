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
| Backend | `src/` na raiz, Spring Boot 3.2, Flyway, `ApiResponse`, exceptions |
| **Auth JWT** | `features/auth/`, login/refresh/me, BCrypt, filtro JWT, `@PreAuthorize` |
| Perfis | `ADMIN`, `OPERADOR`, `VISUALIZADOR` — seed dev no `DataSeeder` |
| Módulo **Cliente** | CRUD back + front, RBAC (delete só ADMIN), testes unitários |
| Frontend `core/` | `auth.service`, interceptors, `authGuard`, `roleGuard` |
| Login | `/login` — redireciona rotas protegidas |
| CORS | `localhost:4200` via `CorsConfig` |
| Actuator | `/actuator/health` público; demais endpoints protegidos |
| **Design System LS1 Light** | Tema claro hospitalar em `styles.css`; playground `/dev/ui` (ADMIN) |
| **App Shell** | Sidebar modular + topbar; `core/layout/app-shell/`; menu em `nav.config.ts` |
| **Toast LS1** | `ToastService` + `app-toast-container` — feedback global sucesso/erro |

**Usuários dev (senha no `DataSeeder`):**

| E-mail | Senha | Perfil |
|--------|-------|--------|
| admin@labsystem.local | admin123 | ADMIN |
| operador@labsystem.local | operador123 | OPERADOR |
| visualizador@labsystem.local | visualizador123 | VISUALIZADOR |

### O que ainda não existe (não inventar no código)

| Área | Pendente |
|------|----------|
| Docker, compose, `.env.example`, `application-prod.yml` | 🔲 |
| traceId/MDC, logs prod refinados | 🔲 |
| Planos SaaS, multi-tenant | 🔲 |
| Módulos Exames, Pedidos, Resultados | 🔲 |
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

- `GlobalExceptionHandler` expõe mensagem interna em 500
- Sem Docker para deploy
- CORS ainda fixo em dev (`localhost:4200`)

### Próxima entrega recomendada

**Módulo Exames (catálogo)** ou **Docker/prod** — conforme priorização da seção 3.

---

## 3. Domínio e priorização

Fluxo de negócio do Labsystem:

```
Cliente → Pedido → Exame(s) → Resultado → Entrega/Cobrança
```

Ordem sugerida de evolução:

1. Autenticação e perfis
2. Cadastros base (clientes, catálogo de exames)
3. Fluxo principal (pedido → resultado)
4. Relatórios e exportação
5. Integrações

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
└── services/tenant-context.service.ts  → empresa (placeholder até multi-tenant)
```

- **Novo módulo:** adicionar item em `nav.config.ts` + rota lazy em `app.routes.ts` (children do shell)
- **Governança:** seção `governanca` — só `ADMIN`; placeholders até implementar
- **Itens `disabled: true`:** visíveis no menu com badge "Em breve" (roadmap)
- **Topbar:** empresa + usuário + perfil; **sidebar:** navegação por módulo
- Login (`/login`) fica **fora** do shell

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

## 6. Usuários, perfis e planos

Obrigatório para produto multi-usuário ou SaaS.

**Perfis iniciais:** `ADMIN`, `OPERADOR`, `VISUALIZADOR`.

- Permissão validada no **backend** (`@PreAuthorize` ou checagem no Service)
- Frontend esconde ação; backend retorna 403 se sem permissão
- JWT: login, refresh, `/auth/me`
- Senha BCrypt; nunca logar nem retornar senha/token

**Planos (quando SaaS):** tabela `planos` com limites e flags de feature. Validar limite no Service:

```java
if (planoService.limiteAtingido(tenantId, "clientes")) {
    throw new BusinessException("Limite do plano atingido.");
}
```

**Multi-tenant:** `tenant_id` em tabelas de negócio; filtrar sempre pelo JWT — nunca confiar no body.

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

---

## 11. Evolução contínua

Ao fechar cada módulo:

1. Atualizar tabelas em `docs/ARQUITETURA.md` (módulos + infraestrutura)
2. Atualizar **seção 2 (Estado atual)** desta skill
3. Manter `features/cliente/` como referência até existir módulo mais completo no DoD

Invocar no Cursor: `/labsystem`

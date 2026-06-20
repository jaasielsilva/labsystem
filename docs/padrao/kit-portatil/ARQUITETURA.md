# Arquitetura do Projeto — {nome-projeto}

> **Molde genérico** — copie para `docs/ARQUITETURA.md` em cada projeto novo e substitua `{placeholders}`.

> Documento de referência obrigatório para todos os desenvolvedores.  
> Siga este padrão em **todos** os módulos e features do projeto.

---

## Stack

| Camada | Tecnologia | Status |
|--------|------------|--------|
| Backend | {ex.: Java 17 + Spring Boot 3.x} | 🔲 |
| Frontend | {ex.: Angular 17+ Standalone} | 🔲 |
| Banco | {ex.: MySQL 8 / PostgreSQL} | 🔲 |
| ORM | {ex.: Spring Data JPA} | 🔲 |
| Migrations | {ex.: Flyway} | 🔲 |
| Segurança | {ex.: JWT + RBAC} | 🔲 |
| Observabilidade | {ex.: Actuator + SLF4J} | 🔲 |
| Container | {ex.: Docker + Compose} | 🔲 |
| Docs API | {ex.: OpenAPI / Swagger} | 🔲 |
| Testes Back | {ex.: JUnit 5 + Mockito} | 🔲 |
| Testes Front | {ex.: Jasmine + Karma / Vitest} | 🔲 |

---

## Estrutura de Pastas

### Backend

```
{repo}/
├── {arquivo-build}                    # pom.xml, package.json, go.mod…
├── src/
│   ├── main/
│   │   ├── {codigo}/
│   │   │   ├── {AppMain}              # entrypoint
│   │   │   ├── config/                # segurança, CORS, beans globais
│   │   │   ├── common/                # ApiResponse, utilitários compartilhados
│   │   │   ├── exception/             # handlers e exceções de domínio
│   │   │   └── features/
│   │   │       ├── auth/              # autenticação
│   │   │       └── {feature-ref}/     # módulo de referência CRUD
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/          # V1__init.sql, V2__…
│   └── test/
└── Dockerfile
```

### Frontend (remover seção se não houver front)

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                      # layout, guards, interceptors, services globais
│   │   │   ├── layout/app-shell/      # shell autenticado (sidebar + topbar)
│   │   │   ├── navigation/            # nav.config.ts
│   │   │   ├── guards/
│   │   │   └── interceptors/ services/
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   └── {feature-ref}/         # referência viva
│   │   └── app.routes.ts              # lazy loading
│   └── environments/
└── package.json
```

> Ajuste a árvore ao stack. Separação fixa: `core/` (infra) vs `features/` (domínio).

---

## Regras Obrigatórias

### Backend

- Pacote raiz: `{pacote.raiz}`
- **Nunca expor Entity/Model interno na API** — sempre DTOs
- Validações na camada de entrada
- Respostas padronizadas: `{ex.: ApiResponse<T>}`
- Logs estruturados — nunca `System.out.println` / `console.log` em prod
- Schema via migrations — nunca alterar banco manualmente em prod
- Multi-tenant (se aplicável): filtrar por `{tenant_id}` do token — nunca do body

### Frontend

- HTTP/API só em services
- Interceptors para auth e erros
- Config de API via `environment.ts` ou `.env`
- Lazy loading por feature
- Multi-tenant: não enviar `{tenantId}` no body

### Banco de Dados

- Migrations somente via ferramenta oficial
- Tabelas `snake_case` plural
- Colunas padrão: `id`, `created_at`, `updated_at`
- FKs explícitas nas migrations
- Multi-tenant: `{tenant_id} NOT NULL` em tabelas de negócio

---

## Padrão de URL

```
GET    /api/v1/{recurso}              → listar (paginado, query `q`)
GET    /api/v1/{recurso}/{id}
POST   /api/v1/{recurso}
PUT    /api/v1/{recurso}/{id}
DELETE /api/v1/{recurso}/{id}
```

Ações de domínio: `POST /api/v1/{recurso}/{id}/{acao}`

Plataforma (SaaS): `/api/v1/platform/{recurso}`

---

## Modelo de domínio

{Descreva entidades, relacionamentos e fluxo do SEU sistema.}

```
{entidade_a}
├── id, …
└── created_at, updated_at

{entidade_b}
├── {entidade_a}_id → FK
└── …
```

### Fluxo de negócio

```
{Etapa 1} → {Etapa 2} → {Etapa 3} → [{Futuro 🔲}]
```

| Etapa | Status | Observação |
|-------|--------|------------|
| {nome} | 🔲 | {nota} |

---

## Multi-tenant (opcional — apagar se não for SaaS)

Tenant = **`{tenant_id}`** (ex.: `empresa_id`). Isolamento via JWT — **nunca** body/query.

| Camada | Implementação |
|--------|----------------|
| JWT | claim `{tenantId}`, escopos (`PLATFORM`, `TENANT`…) |
| Backend | `{TenantContext}` |
| Repository | `findByIdAnd{Tenant}Id`, `findAllBy{Tenant}Id` |
| Frontend | guard + service de contexto |

### Filtros JPA — hoje vs escala (Java/Hibernate)

| Aspecto | Fase 1 | Escala |
|---------|--------|--------|
| Estratégia | Filtro explícito no repository | `@TenantId`, `@Filter` / `@FilterDef` |
| Status | 🔲 | 🔲 com dezenas de tabelas |

> Até migrar: todo repository novo com `*By{Tenant}Id`.

### Fases SaaS

| Fase | Escopo | Status |
|------|--------|--------|
| 1 — Tenant | `{tenant_id}`, JWT, contexto | 🔲 |
| 2 — Plataforma | Admin global, onboarding | 🔲 |
| 3 — Operacional | Fluxo principal | 🔲 |
| 4 — Comercial | Planos, billing | 🔲 |

---

## Módulos do Projeto

| Módulo | Backend | Frontend | Tenant |
|--------|---------|----------|--------|
| Setup base | 🔲 | 🔲 | — |
| Autenticação | 🔲 | 🔲 | — |
| {feature-ref} (referência) | 🔲 | 🔲 | 🔲 |
| {próximo módulo} | 🔲 | 🔲 | — |

> Atualize ao fechar cada módulo. DoD: skill `/{nome-projeto}` seção 9.

---

## Infraestrutura

| Item | Status |
|------|--------|
| Docker / Compose | 🔲 |
| Config produção | 🔲 |
| Health check | 🔲 |
| traceId / correlation ID | 🔲 |
| OpenAPI / Swagger | 🔲 |

---

## Padrão Empresarial

Skill mestre: [`docs/skills/{nome-projeto}/SKILL.md`](./skills/{nome-projeto}/SKILL.md) — Cursor: `/{nome-projeto}`

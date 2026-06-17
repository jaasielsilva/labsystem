# Arquitetura do Projeto — labsystem

> Documento de referência obrigatório para todos os desenvolvedores.  
> Siga este padrão em **todos** os módulos e features do projeto.

---

## Stack

| Camada       | Tecnologia                          | Status   |
|--------------|-------------------------------------|----------|
| Backend      | Java 17 + Spring Boot 3.2           | ✅ Ativo |
| Frontend     | Angular 17+ (Standalone Components) | ✅ Ativo |
| Banco        | MySQL 8                             | ✅ Ativo |
| ORM          | Spring Data JPA + Hibernate         | ✅ Ativo |
| Migrations   | Flyway (V1–V9)                      | ✅ Ativo |
| Segurança    | Spring Security + JWT + RBAC        | ✅ Ativo |
| Observabilidade | Actuator + SLF4J                 | ✅ health exposto |
| Container    | Docker + Docker Compose             | 🔲 Pendente |
| Docs API     | SpringDoc OpenAPI (Swagger UI)      | 🔲 Pendente |
| Testes Back  | JUnit 5 + Mockito                   | ✅ Ativo |
| Testes Front | Jasmine + Karma                     | ✅ Configurado |

---

## Estrutura de Pastas

### Backend (raiz do repositório)

```
labsystem/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/jaasielsilva/labsystem/
│   │   │   ├── LabsystemApplication.java
│   │   │   ├── config/                    ✅ Security, JWT, CORS
│   │   │   ├── common/                    ✅ ApiResponse, TenantContext, AccessScope
│   │   │   ├── exception/                 ✅
│   │   │   └── features/
│   │   │       ├── auth/                  ✅
│   │   │       ├── cliente/             ✅ referência CRUD tenant
│   │   │       ├── exame/                 ✅
│   │   │       ├── pedido/                ✅
│   │   │       ├── resultado/             ✅
│   │   │       ├── empresa/               ✅ entidade + DTOs
│   │   │       ├── usuario/               ✅ governança tenant
│   │   │       ├── platform/              ✅ SUPER_ADMIN, onboarding, impersonação
│   │   │       └── tenant/security/       ✅ TenantAccessEvaluator
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__init.sql … V7__super_admin_platform.sql
│   │           ├── V8__create_table_pedidos.sql
│   │           └── V9__create_table_resultados.sql
│   └── test/java/.../features/*/service/  ✅
└── Dockerfile                             🔲 pendente
```

### Frontend

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                          ✅
│   │   │   ├── layout/app-shell/          ✅ sidebar + topbar
│   │   │   ├── navigation/                ✅ nav.config.ts
│   │   │   ├── guards/                    ✅ auth, tenant, superAdmin, role
│   │   │   ├── interceptors/ services/
│   │   ├── features/
│   │   │   ├── auth/                      ✅ login
│   │   │   ├── cliente/ exame/ pedido/ resultado/  ✅ operacional tenant
│   │   │   ├── governanca/usuario/        ✅ ADMIN tenant
│   │   │   ├── plataforma/                ✅ laboratorios, usuarios globais
│   │   │   └── governanca/empresa/        ✅ formulário laboratório (plataforma)
│   │   └── app.routes.ts                  ✅ lazy loading
│   └── environments/
├── Dockerfile                             🔲 pendente
└── package.json
```

---

## Regras Obrigatórias

### Backend

- Pacote raiz: `com.jaasielsilva.labsystem`
- **Nunca expor Entity na API** — sempre DTOs
- Validações em Request DTO (`jakarta.validation`)
- Respostas com `ApiResponse<T>`
- Logs SLF4J — nunca `System.out.println`
- `ddl-auto: validate` em produção
- **Todo módulo de negócio:** filtrar por `empresa_id` do JWT via `TenantContext`

### Frontend

- Standalone Components
- HttpClient só em services
- Interceptors JWT + erros
- `environment.ts` para API URL
- Reactive Forms
- Lazy loading por feature
- **Não enviar `empresaId` no body** — tenant vem do token

### Banco de Dados

- Migrations **somente Flyway**
- Tabelas `snake_case` plural (`clientes`, `pedido_itens`, `resultados`)
- Colunas padrão: `id`, `created_at`, `updated_at` (onde aplicável)
- FKs explícitas nas migrations
- **Negócio:** `empresa_id NOT NULL` → `empresas` (ver Multi-tenant)

---

## Padrão de URL

### Tenant (laboratório)

```
GET    /api/v1/{recurso}              → listar (paginado, query `q`)
GET    /api/v1/{recurso}/{id}
POST   /api/v1/{recurso}
PUT    /api/v1/{recurso}/{id}
DELETE /api/v1/{recurso}/{id}
```

Exemplos: `/clientes`, `/exames`, `/pedidos`, `/resultados`, `/usuarios`

Ações de domínio (quando necessário): `POST /pedidos/{id}/concluir`, `POST /resultados/{id}/liberar`

### Plataforma (SUPER_ADMIN)

```
/api/v1/platform/empresas
/api/v1/platform/usuarios
/api/v1/platform/impersonate/{laboratorioId}
```

---

## Multi-tenant por empresa

O sistema **já é multi-tenant**: cada laboratório (`empresas.tipo = LABORATORIO`) é um tenant isolado por `empresa_id`.

> **Não confundir** isolamento por empresa (implementado) com **planos e billing** (Fase 4 — futuro).

### Modelo de dados atual

```
empresas
├── id, nome, cnpj, tipo (PLATAFORMA | LABORATORIO), …
└── created_at, updated_at

usuarios
├── empresa_id → FK empresas (NOT NULL)
└── perfil (SUPER_ADMIN | ADMIN | OPERADOR | VISUALIZADOR)

clientes | exames | pedidos | resultados
├── empresa_id → FK empresas (NOT NULL)
└── …

pedido_itens
├── pedido_id → pedidos
├── exame_id → exames
└── (isolamento via pedido.empresa_id)

resultados
├── empresa_id → FK empresas
├── pedido_item_id → UK (1 resultado por item)
└── status, laudo, data_liberacao
```

- No código e migrations: **`empresa_id`** = identificador do tenant (sinônimo de `tenant_id`).

### Isolamento em runtime

| Camada | Implementação |
|--------|----------------|
| JWT | `empresaId`, `scope`, `actingEmpresaId` (impersonação) |
| Backend | `TenantContext.requireTenantEmpresaId()` |
| Autorização tenant | `@tenantAccess.read()` / `.write()` / `.admin()` |
| Autorização plataforma | `@platformAccess.allow()` |
| Repository | `findByIdAndEmpresaId`, `findAllByEmpresaId`, … |
| Frontend | `tenantGuard`, `hasTenantAccess()`, `TenantContextService` |

**Regra de ouro:** nunca usar `empresaId` do body ou query string para decidir qual tenant acessar.

### Perfis e escopos

| Perfil | Escopo | API principal | Front |
|--------|--------|---------------|-------|
| `SUPER_ADMIN` | Plataforma | `/api/v1/platform/**` | `/plataforma/*` |
| `SUPER_ADMIN` (impersonação) | Tenant suporte | `/api/v1/*` com `actingEmpresaId` | operacional + banner |
| `ADMIN` | Tenant | `/api/v1/*` | operacional + `/governanca/*` |
| `OPERADOR` / `VISUALIZADOR` | Tenant | leitura/escrita conforme RBAC | operacional |

JWT `scope`: `PLATFORM`, `TENANT`, `TENANT_IMPERSONATION`

### Evolução em fases

| Fase | Escopo | Status |
|------|--------|--------|
| **1 — Tenant** | `empresa_id`, JWT, `TenantContext`, isolamento em services | ✅ |
| **2 — Plataforma** | SUPER_ADMIN, laboratórios, usuários globais, onboarding, impersonação | ✅ |
| **3 — Operacional** | Pedidos, Resultados | ✅ |
| **4 — Comercial** | Planos, limites por recurso, billing/assinatura | 🔲 |

#### Fase 4 — Planos + billing (roadmap, sem código)

Previsto para monetização SaaS — **ortogonal** ao `empresa_id`:

- Tabela `planos` (limites: usuários, clientes, pedidos/mês, módulos)
- Vínculo empresa ↔ plano/assinatura
- Checagem `limiteAtingido(empresaId, recurso)` nos services de criação
- Integração com gateway de pagamento
- UI de uso vs limite e upgrade

Até a Fase 4, **todos os laboratórios têm acesso ilimitado** ao que está implementado.

---

## Módulos do Projeto

| Módulo | Backend | Frontend | Tenant-aware |
|--------|---------|----------|--------------|
| Setup base | ✅ | ✅ | — |
| Autenticação JWT + perfis | ✅ | ✅ | — |
| Clientes | ✅ | ✅ | ✅ |
| Exames (catálogo) | ✅ | ✅ | ✅ |
| Pedidos | ✅ | ✅ | ✅ |
| Resultados | ✅ | ✅ | ✅ |
| Usuários (governança tenant) | ✅ | ✅ | ✅ |
| Plataforma (laboratórios, usuários globais) | ✅ | ✅ | escopo SUPER_ADMIN |
| Onboarding lab + admin | ✅ | ✅ | plataforma |
| Impersonação | ✅ | ✅ | plataforma → tenant |
| Relatórios | 🔲 | 🔲 | — |
| Planos + billing | 🔲 | 🔲 | — |

> Atualize esta tabela ao fechar cada módulo. Checklist completo: skill `/labsystem` seção 9.

---

## Fluxo de negócio implementado

```
Cliente → Pedido (N exames) → Resultado (por item) → [Entrega/Cobrança 🔲]
```

| Etapa | Status | Observação |
|-------|--------|------------|
| Cadastro cliente | ✅ | FK obrigatória no pedido |
| Pedido multi-exame | ✅ | `pedido_itens` |
| Laudo por exame | ✅ | `resultados` 1:1 com item |
| Status pedido automático | ✅ | `EM_ANDAMENTO` / `CONCLUIDO` via resultados |
| Cobrança / entrega | 🔲 | Fase 4+ |

---

## Infraestrutura

| Item | Status |
|------|--------|
| Docker / Compose | 🔲 |
| `application-prod.yml` | 🔲 |
| Actuator `/health` | ✅ |
| traceId (MDC) | 🔲 |
| OpenAPI / Swagger | 🔲 |

---

## Padrão Empresarial

Toda feature deve seguir a skill mestre em [`docs/skills/labsystem/SKILL.md`](./skills/labsystem/SKILL.md) (produto, UX, segurança, deploy, observabilidade). Invocar no Cursor: `/labsystem`.

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
| Migrations   | Flyway                              | ✅ Ativo |
| Segurança    | Spring Security + JWT               | ✅ JWT + RBAC |
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
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java      ✅
│   │   │   │   ├── JwtService.java          ✅
│   │   │   │   ├── JwtAuthenticationFilter.java ✅
│   │   │   │   └── CorsConfig.java          ✅
│   │   │   ├── exception/                   ✅
│   │   │   ├── common/ApiResponse.java      ✅
│   │   │   └── features/
│   │   │       ├── auth/                    ✅ JWT
│   │   │       ├── cliente/                 ✅ referência CRUD
│   │   │       ├── empresa/                 ✅ governança + tenant
│   │   │       └── {feature}/               ← novo módulo
│   │   │           ├── entity/ repository/ dto/ mapper/
│   │   │           ├── service/impl/ controller/
│   │   └── resources/
│   │       ├── application.yml              ✅
│   │       └── db/migration/
│   │           ├── V1__init.sql             ✅
│   │           └── V2__create_table_clientes.sql ✅
│   └── test/java/.../features/{feature}/service/ ✅
└── Dockerfile                               🔲 pendente
```

### Frontend

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                            ✅
│   │   │   ├── components/toast-container/  ✅ feedback global
│   │   │   ├── layout/app-shell/            ✅ sidebar + topbar
│   │   │   ├── navigation/                  ✅ menu modular
│   │   │   ├── interceptors/ guards/ services/ (incl. toast.service)
│   │   ├── features/auth/pages/login/       ✅
│   │   ├── shared/
│   │   │   └── models/api-response.model.ts ✅
│   │   ├── features/
│   │   │   ├── cliente/                     ✅ referência
│   │   │   ├── governanca/empresa/          ✅
│   │   │   └── {feature}/
│   │   │       ├── models/ services/ pages/ {feature}.routes.ts
│   │   ├── app.component.ts
│   │   ├── app.config.ts                    ✅ (interceptors 🔲)
│   │   └── app.routes.ts                    ✅ lazy por feature
│   └── environments/
│       ├── environment.ts                   ✅
│       └── environment.prod.ts              ✅
├── Dockerfile                               🔲 pendente
└── package.json
```

### Raiz

```
labsystem/
├── src/                         ← backend Spring Boot (não há pasta backend/)
├── frontend/
├── docs/
│   ├── ARQUITETURA.md
│   └── skills/labsystem/SKILL.md
├── pom.xml
├── .cursor/skills/              → junction para docs/skills
├── docker-compose.yml           🔲 pendente
├── docker-compose.prod.yml      🔲 pendente
├── .env.example                 🔲 pendente
└── .env                         ← nunca commitar
```

---

## Regras Obrigatórias

### Backend

- Pacote raiz: `com.jaasielsilva.labsystem`
- **Nunca expor a Entity diretamente na API** — sempre usar DTOs
- Validações via `jakarta.validation` nas classes de Request DTO
- Respostas sempre com o wrapper `ApiResponse<T>`
- Logs com SLF4J — nunca `System.out.println`
- Variáveis sensíveis sempre em `application.yml` via variáveis de ambiente
- `ddl-auto: validate` em produção — nunca `create` ou `update`

### Frontend

- Sempre **Standalone Components** — sem NgModules
- HttpClient centralizado em services — nunca em componentes diretamente
- Interceptors para JWT e erros globais
- URLs de API via `environment.ts`
- Reactive Forms para formulários
- Lazy loading por rota de feature

### Banco de Dados

- Migrations **sempre via Flyway** — nunca alterar banco manualmente
- Tabelas em `snake_case` plural (ex: `clientes`, `pedido_itens`)
- Toda tabela deve ter: `id`, `created_at`, `updated_at`
- Foreign keys explícitas no SQL de migration
- Tabelas de **negócio** devem ter `empresa_id NOT NULL` referenciando `empresas` (ver seção Multi-tenant)

### Docker

- Multi-stage build em todos os Dockerfiles
- Variáveis sensíveis via `.env` — nunca hardcoded no compose
- Healthcheck nos serviços `db` e `backend`
- Redes internas nomeadas

---

## Fluxo de Criação de Módulo

Ao criar uma nova feature, siga **sempre** esta ordem:

```
1. Entity (JPA)
2. Migration SQL (Flyway)
3. Repository (interface)
4. DTOs (Request + Response)
5. Service (interface + impl)
6. Controller (REST)
7. Testes unitários do Service
8. Model Angular
9. Service Angular
10. Page + Form Angular (standalone)
11. Rota lazy no app.routes.ts
```

---

## Padrão de Resposta da API

Toda resposta da API usa o wrapper abaixo — nunca retornar objeto cru:

```java
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
```

---

## Padrão de URL

```
GET    /api/v1/{recurso}         → listar (paginado)
GET    /api/v1/{recurso}/{id}    → buscar por ID
POST   /api/v1/{recurso}        → criar
PUT    /api/v1/{recurso}/{id}    → atualizar
DELETE /api/v1/{recurso}/{id}   → deletar
```

---

## Multi-tenant e empresas

O Labsystem evolui para SaaS em **três fases** (detalhes na skill `/labsystem`, seção 6.1). Hoje o código ainda é **mono-tenant**; a fundação abaixo é o **alvo obrigatório** antes de Pedidos/Resultados.

### Modelo de dados (alvo)

```
empresas
├── id, nome, cnpj, email, …
└── created_at, updated_at

usuarios
├── empresa_id  → FK empresas (NOT NULL)
└── …

clientes | exames | pedidos | …
├── empresa_id  → FK empresas (NOT NULL)
└── …
```

- No código e nas migrations usar **`empresa_id`** (equivalente conceitual a `tenant_id`).
- Tabela `empresas` é criada na **Fase 1**; CRUD administrativo na **Fase 2** (`/api/v1/empresas`, front `/governanca/empresas`).

### Isolamento (Fase 1 — tenant-ready)

| Camada | Regra |
|--------|--------|
| JWT | Claim `empresaId` no access/refresh token |
| `/auth/me` | Retorna `empresaId` e `empresaNome` |
| Service | Toda operação de negócio filtra pelo `empresaId` do JWT |
| API | **Nunca** usar `empresaId` do body para autorizar ou filtrar |
| Frontend | `TenantContextService` preenchido pelo login (`/auth/me`) |
| Seed dev | Uma empresa (`Laboratório Demo`); dados legados migrados para ela |

### Fases (resumo)

| Fase | Escopo | Status |
|------|--------|--------|
| **1 — Tenant-ready** | `empresas`, `empresa_id`, JWT, filtro nos services | ✅ |
| **2 — Governança** | CRUD Empresa + vínculo usuário ↔ empresa | ✅ |
| **3 — SaaS** | Planos, limites, onboarding multi-laboratório | 🔲 |

### Perfis e escopos

| Perfil | Escopo | API | Front |
|--------|--------|-----|-------|
| `SUPER_ADMIN` | Plataforma | `/api/v1/platform/**` | `/plataforma/*` |
| `ADMIN` | Tenant (laboratório) | `/api/v1/*` filtrado por JWT | Operacional + governança tenant |
| `OPERADOR` / `VISUALIZADOR` | Tenant | leitura/escrita conforme RBAC | Operacional |

- JWT claim `scope`: `PLATFORM` ou `TENANT`
- Empresa `tipo`: `PLATAFORMA` (sentinela) ou `LABORATORIO`

### O que **não** confundir

- **Plataforma (SUPER_ADMIN):** gerencia laboratórios e usuários globais.
- **Tenant (ADMIN):** gerencia usuários e dados do próprio laboratório.

---

## Módulos do Projeto

| Módulo | Backend | Frontend | DoD empresarial |
|--------|---------|----------|-----------------|
| Setup base (ApiResponse, exceptions, Flyway) | ✅ | ✅ | ✅ |
| Autenticação (JWT + perfis) | ✅ | ✅ | ✅ |
| **Fundação tenant-ready** (seção Multi-tenant) | ✅ | ✅ | ✅ |
| Clientes | ✅ | ✅ | ✅ |
| Empresas (governança) | ✅ | ✅ | ✅ |
| Exames (catálogo) | ✅ | ✅ | ✅ |
| Pedidos | 🔲 | 🔲 | 🔲 |
| Resultados | 🔲 | 🔲 | 🔲 |
| SaaS (planos, limites) | 🔲 | 🔲 | 🔲 |

> Atualize esta tabela ao fechar cada módulo. "DoD empresarial" = checklist da skill `/labsystem`.

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

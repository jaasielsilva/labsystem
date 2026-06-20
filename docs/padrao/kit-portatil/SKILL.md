---
name: {nome-projeto}
description: Padrão empresarial do {nome-projeto}. Use em QUALQUER desenvolvimento — feature, correção, refactor, deploy ou revisão. Produto vendável, código consistente, pronto para produção.
---

# {Nome Exibido} — Padrão Empresarial

> **Molde genérico** — copie para `docs/skills/{nome-projeto}/SKILL.md` e `.cursor/skills/{nome-projeto}/SKILL.md`. Substitua `{placeholders}`.

Skill mestre do projeto. Siga **sempre** ao desenvolver, revisar ou planejar.

Documento técnico: `docs/ARQUITETURA.md`.

---

## 1. Princípio central

Construir um **produto vendável**, não só CRUDs corretos.

| Pilar | Pergunta-chave |
|-------|----------------|
| **Produto** | Qual dor real isso resolve? |
| **Arquitetura** | Segue o padrão do projeto? |
| **UX** | O usuário consegue usar sem ajuda? |
| **Segurança** | Quem pode fazer o quê? |
| **Deploy** | Sobe em produção sem surpresa? |
| **Observabilidade** | Dá para debugar em prod? |

Se um pilar ficar de fora, a feature **não está pronta**.

> **Docs = alvo + estado real.** 🔲 = ainda não existe no código.

---

## 2. Estado atual do projeto

**Atualizar ao fechar cada entrega.**

### O que já existe

| Área | Implementado |
|------|----------------|
| Documentação | ARQUITETURA + skill |
| Backend | 🔲 |
| Frontend | 🔲 |
| Auth | 🔲 |
| {feature-ref} (CRUD ref) | 🔲 |

### O que ainda não existe (não inventar)

| Área | Pendente |
|------|----------|
| {módulo} | 🔲 |

### Referência viva

- CRUD: `features/{feature-ref}/` (back e front)
- Auth: `features/auth/` + `core/`

### Design System — {nome-tema} (quando houver front)

| Token | Valor | Uso |
|-------|-------|-----|
| `--color-primary` | `{#hex}` | Botões, marca |
| `--color-accent` | `{#hex}` | Links, destaques |
| `--bg-page` | `{#hex}` | Fundo |
| `--card-bg` | `{#hex}` | Cards |

### Próxima entrega

**Setup base + auth + {feature-ref}** — seção 3.

---

## 3. Domínio e priorização

```
{Etapa 1} → {Etapa 2} → {Etapa 3} → {Futuro}
```

Ordem sugerida:

1. Auth + perfis
2. Tenant (se SaaS)
3. Cadastros base
4. Fluxo principal
5. Relatórios
6. Deploy
7. Billing (se SaaS)
8. Integrações

### Antes de codar

1. Quem usa?
2. Qual dor resolve?
3. Como medir sucesso?
4. MVP vs v2?

---

## 4. Fluxo de nova feature

Ordem fixa:

```
BACKEND:  Entity → Migration → Repository → DTOs → Service → Controller → Testes
FRONTEND: Model → Service → Pages/Form → Rota lazy
```

### Backend (`{pacote.raiz}`)

```
features/{feature}/entity/ repository/ dto/ mapper/ service/impl/ controller/
```

- DTOs na API, nunca Entity
- Validação na entrada
- Resposta padronizada
- Migration: `V{n}__descricao.sql`
- Multi-tenant: `{tenant_id}` + filtro via token

### Frontend

```
features/{feature}/models/ services/ pages/ {feature}.routes.ts
```

- HttpClient só em services
- Referência: `features/{feature-ref}/`

---

## 5. UX mínima (toda tela)

| Estado | Comportamento |
|--------|---------------|
| Loading | Spinner; desabilitar ações duplicadas |
| Empty | Mensagem + CTA |
| Error | Toast claro — sem stack trace |
| Success | Feedback após ação |

- Formulários: validação após `touched`
- Listagens: busca `q`, paginação, confirmar exclusão
- Dados sensíveis mascarados (LGPD)
- Textos em português claro

---

## 6. Usuários, perfis e tenant (se aplicável)

- Permissão no **backend** — front só esconde UI
- Senha com hash — nunca logar token/senha
- Multi-tenant: isolamento por token; `{tenant_id} NOT NULL` em negócio
- Repository: filtro explícito até escala JPA (`ARQUITETURA.md`)

---

## 7. Logs e monitoramento

- INFO / WARN / ERROR conforme tipo de falha
- Nunca logar PII, senha ou token em prod
- Health endpoint para deploy
- 500 genérico ao cliente

---

## 8. Deploy e produção

- Docker multi-stage; secrets em `.env`
- Migrations na subida
- CORS restrito
- Pipeline: test → build → deploy → smoke health

---

## 9. Definition of Done

- [ ] Dor real resolvida; MVP definido
- [ ] Fluxo seção 4 completo + teste Service
- [ ] Loading, empty, error, success
- [ ] Endpoint protegido; sem secrets hardcoded
- [ ] `ARQUITETURA.md` atualizado

---

## 10. Anti-padrões

- CRUD sem jornada de uso
- Permissão só no front
- Banco manual sem migration
- Query sem filtro de tenant (se SaaS)
- Feature sem justificativa de produto

---

## 11. Evolução contínua

Ao fechar módulo:

1. `ARQUITETURA.md` — tabela de módulos
2. Esta skill — seção 2
3. Sincronizar `.cursor/skills/{nome-projeto}/SKILL.md`

Invocar no Cursor: `/{nome-projeto}`

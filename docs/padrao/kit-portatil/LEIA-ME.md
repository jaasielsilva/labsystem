# Kit Portátil — ARQUITETURA + SKILL

Dois arquivos genéricos para **qualquer sistema** que você criar.  
Copie esta pasta inteira para o seu PC e **nunca edite os originais** — sempre copie do kit para cada projeto novo.

---

## Onde guardar no PC (sugestão)

```
C:\Users\jasie\Documentos\MeusTemplates\padrao-projeto\
├── LEIA-ME.md          ← este guia
├── ARQUITETURA.md      ← molde genérico (NÃO editar aqui)
├── SKILL.md            ← molde genérico (NÃO editar aqui)
└── iniciar-projeto.ps1 ← script opcional
```

**Como “baixar”:** copie a pasta `kit-portatil/` do Labsystem para `MeusTemplates\padrao-projeto\` (ou OneDrive, pendrive, etc.).

---

## Os 2 arquivos que importam

| Arquivo no kit | Vira no projeto novo | Função |
|----------------|----------------------|--------|
| `ARQUITETURA.md` | `docs/ARQUITETURA.md` | Stack, pastas, regras, módulos |
| `SKILL.md` | `docs/skills/{nome}/SKILL.md` + `.cursor/skills/{nome}/SKILL.md` | Como codar, UX, DoD — IA usa com `/{nome}` |

---

## A cada projeto novo (checklist)

```
[ ] 1. Criar pasta do repo (git init)
[ ] 2. Copiar ARQUITETURA.md → docs/ARQUITETURA.md
[ ] 3. Copiar SKILL.md → docs/skills/meuapp/SKILL.md
[ ] 4. Copiar SKILL.md → .cursor/skills/meuapp/SKILL.md  (mesmo conteúdo)
[ ] 5. Substituir {placeholders} nos dois arquivos (buscar "{")
[ ] 6. Abrir no Cursor
[ ] 7. Prompt: "/meuapp Leia os docs e crie setup base + auth + 1º CRUD referência"
[ ] 8. A cada módulo fechado: atualizar ARQUITETURA + skill seção 2
```

---

## Script rápido (PowerShell)

Na pasta do **projeto novo**:

```powershell
# Ajuste estes 3 valores:
$kit    = "C:\Users\jasie\Documentos\MeusTemplates\padrao-projeto"
$dest   = "C:\Users\jasie\OneDrive\Documentos\Projetos Pessoais\meu-novo-app"
$nome   = "meuapp"   # slug: minúsculo, sem espaço (vai no /meuapp do Cursor)

New-Item -ItemType Directory -Force -Path "$dest\docs\skills\$nome"
New-Item -ItemType Directory -Force -Path "$dest\.cursor\skills\$nome"
Copy-Item "$kit\ARQUITETURA.md" "$dest\docs\ARQUITETURA.md"
Copy-Item "$kit\SKILL.md"       "$dest\docs\skills\$nome\SKILL.md"
Copy-Item "$kit\SKILL.md"       "$dest\.cursor\skills\$nome\SKILL.md"
Write-Host "Pronto! Edite {placeholders} em docs/ e abra no Cursor com /$nome"
```

Ou use: `.\iniciar-projeto.ps1 -Nome meuapp -Destino C:\...\meu-novo-app`

---

## Placeholders para trocar (buscar `{` no editor)

| Placeholder | Exemplo jurídico | Exemplo clínica |
|-------------|------------------|-----------------|
| `{nome-projeto}` | juridico | clinica |
| `{Nome Exibido}` | Jurídico | Clínica |
| `{pacote.raiz}` | com.jaasielsilva.juridico | com.jaasielsilva.clinica |
| `{feature-ref}` | cliente | paciente |
| `{tenant_id}` | empresa_id | empresa_id |

Se **não** for multi-tenant: apague a seção "Multi-tenant" nos dois arquivos.

Se **não** tiver frontend: apague seções Frontend e Design System.

---

## Primeiro prompt no Cursor (copiar e adaptar)

```
/{nome-projeto}

Leia docs/ARQUITETURA.md e docs/skills/{nome-projeto}/SKILL.md.
Preencha o que faltar nos docs conforme minhas respostas abaixo.

Domínio: {descreva em 1 frase}
Stack: {Java+Angular / Node+React / etc.}

Crie setup base + auth + módulo {feature-ref} como CRUD referência.
Siga a ordem da seção 4 da skill. Marque 🔲→✅ nos docs ao concluir cada parte.
```

---

## Manutenção do kit (no PC)

- **Não** versionar projetos dentro do kit — só os 2 moldes + este guia
- Se melhorar o padrão em um projeto maduro (ex.: Labsystem), copie as melhorias **de volta** para o kit no PC
- Projetos específicos (jurídico, clínica…) = preencher placeholders; o kit continua genérico

---

## Referência de projeto maduro

Labsystem (`docs/ARQUITETURA.md` + `docs/skills/labsystem/SKILL.md`) — exemplo preenchido do mesmo padrão.

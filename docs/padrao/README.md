# Padrão de Documentação — ARQUITETURA + SKILL

## Kit portátil (use este)

Pasta para **guardar no PC** e reutilizar em qualquer sistema:

```
docs/padrao/kit-portatil/
├── LEIA-ME.md          ← guia completo
├── ARQUITETURA.md      ← molde genérico
├── SKILL.md            ← molde genérico
└── iniciar-projeto.ps1 ← copia para projeto novo
```

**Copie `kit-portatil/` para:**  
`C:\Users\jasie\Documentos\MeusTemplates\padrao-projeto\`

Nunca edite os originais no PC — sempre **copie do kit** para cada repo novo.

---

## Divisão dos 2 documentos

| Documento | Função |
|-----------|--------|
| **ARQUITETURA.md** | Stack, pastas, regras, URLs, domínio, módulos |
| **SKILL.md** | Produto, fluxo de feature, UX, DoD, anti-padrões — IA usa com `/{nome}` |

---

## Referências

| Tipo | Onde |
|------|------|
| Kit genérico | [`kit-portatil/`](./kit-portatil/) |
| Projeto maduro (exemplo preenchido) | Labsystem `docs/ARQUITETURA.md` + `docs/skills/labsystem/SKILL.md` |
| Cópia no PC | `MeusTemplates\padrao-projeto\` |

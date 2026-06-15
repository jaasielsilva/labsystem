/** Máscara LGPD para exibição em listagens — não usar em formulários de edição */
export function maskCpf(cpf: string | undefined | null): string {
  if (!cpf) {
    return '-';
  }

  const digits = cpf.replace(/\D/g, '');
  if (digits.length !== 11) {
    return '***.***.***-**';
  }

  return `${digits.slice(0, 3)}.***.***-${digits.slice(9, 11)}`;
}

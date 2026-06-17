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

/** Formata CPF completo para formulários: 000.000.000-00 */
export function formatCpf(cpf: string | undefined | null): string {
  const digits = digitsOnlyCpf(cpf);
  if (!digits) {
    return '';
  }

  return digits
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1-$2');
}

export function digitsOnlyCpf(value: string | undefined | null): string {
  return (value ?? '').replace(/\D/g, '').slice(0, 11);
}

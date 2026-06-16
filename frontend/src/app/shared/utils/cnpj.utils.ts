/** Máscara LGPD para exibição em listagens — não usar em formulários de edição */
export function maskCnpj(cnpj: string | undefined | null): string {
  if (!cnpj) {
    return '-';
  }

  const digits = cnpj.replace(/\D/g, '');
  if (digits.length !== 14) {
    return '**.***.***/****-**';
  }

  return `${digits.slice(0, 2)}.***.***/${digits.slice(8, 12)}-${digits.slice(12, 14)}`;
}

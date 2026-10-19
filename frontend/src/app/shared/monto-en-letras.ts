/** Convierte un monto a letras, como en los comprobantes peruanos: "Son: ciento veinte con 50/100 soles". */
const UNIDADES = [
  '', 'uno', 'dos', 'tres', 'cuatro', 'cinco', 'seis', 'siete', 'ocho', 'nueve', 'diez',
  'once', 'doce', 'trece', 'catorce', 'quince', 'dieciséis', 'diecisiete', 'dieciocho', 'diecinueve', 'veinte',
  'veintiuno', 'veintidós', 'veintitrés', 'veinticuatro', 'veinticinco', 'veintiséis', 'veintisiete', 'veintiocho', 'veintinueve',
];
const DECENAS = ['', '', '', 'treinta', 'cuarenta', 'cincuenta', 'sesenta', 'setenta', 'ochenta', 'noventa'];
const CENTENAS = ['', 'ciento', 'doscientos', 'trescientos', 'cuatrocientos', 'quinientos', 'seiscientos', 'setecientos', 'ochocientos', 'novecientos'];

function menorQueMil(n: number): string {
  if (n === 100) return 'cien';
  const c = Math.floor(n / 100);
  const r = n % 100;
  const resto = r < 30 ? UNIDADES[r] : DECENAS[Math.floor(r / 10)] + (r % 10 ? ' y ' + UNIDADES[r % 10] : '');
  return [CENTENAS[c], resto].filter(Boolean).join(' ');
}

/** "uno" se apocopa antes de "mil" o "millones": veintiún mil, un millón. */
function apocopar(texto: string): string {
  return texto.replace(/veintiuno$/, 'veintiún').replace(/uno$/, 'un');
}

function enLetras(n: number): string {
  if (n === 0) return 'cero';
  const millones = Math.floor(n / 1_000_000);
  const miles = Math.floor((n % 1_000_000) / 1000);
  const resto = n % 1000;
  const partes: string[] = [];
  if (millones) partes.push(millones === 1 ? 'un millón' : `${apocopar(menorQueMil(millones))} millones`);
  if (miles) partes.push(miles === 1 ? 'mil' : `${apocopar(menorQueMil(miles))} mil`);
  if (resto) partes.push(menorQueMil(resto));
  return partes.join(' ');
}

export function montoEnLetras(monto: number): string {
  const centimos = Math.round(monto * 100);
  const entero = Math.floor(centimos / 100);
  const decimales = String(centimos % 100).padStart(2, '0');
  return `Son: ${enLetras(entero)} con ${decimales}/100 soles`;
}

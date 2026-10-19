export const environment = {
  production: true,
  // La reemplaza Render al compilar con la variable API_URL (ver render.yaml)
  apiUrl: '__API_URL__',
  nombreEmpresa: 'Melamincorp',
  // Número de WhatsApp con código de país, sin + ni espacios
  whatsapp: '51999999999',
  // Datos que se muestran en el pie de página (cámbialos por los reales)
  direccion: 'Ate Vitarte, Lima',
  horario: 'Lunes a sábado, 8:00 a. m. a 6:00 p. m.',
  // Datos para pagos manuales (se muestran al cliente)
  yapePlin: { numero: '999 999 999', titular: 'Melamincorp S.A.C.' },
  cuentaBancaria: { banco: 'BCP', numero: '000-0000000-0-00', cci: '00200000000000000000', titular: 'Melamincorp S.A.C.' },
  // Llave pública de Culqi; mientras diga REEMPLAZAR se usa el pago de prueba  
};
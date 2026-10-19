export const environment = {
  production: true,
  apiUrl: 'https://api.tudominio.pe',
  nombreEmpresa: 'Melamincorp',
  // Número de WhatsApp con código de país, sin + ni espacios
  whatsapp: '51999999999',
  // Datos para pagos manuales (se muestran al cliente)
  yapePlin: { numero: '999 999 999', titular: 'Melamincorp S.A.C.' },
  cuentaBancaria: { banco: 'BCP', numero: '000-0000000-0-00', cci: '00200000000000000000', titular: 'Melamincorp S.A.C.' },
  // Llave pública de Culqi (pk_test_... en pruebas)
  culqiPublicKey: 'pk_live_REEMPLAZAR',
};

export type TipoProducto = 'PLANCHA' | 'SERVICIO';
export type EstadoPedido = 'PENDIENTE_PAGO' | 'PAGADO' | 'EN_PREPARACION' | 'ENVIADO' | 'ENTREGADO' | 'CANCELADO';
export type MetodoPago = 'TARJETA' | 'YAPE_PLIN' | 'TRANSFERENCIA';
export type Rol = 'ADMIN' | 'VENTAS' | 'ALMACEN' | 'OPERARIO';

export const ROLES: Record<Rol, { nombre: string; descripcion: string }> = {
  ADMIN: { nombre: 'Administrador', descripcion: 'Acceso total, incluida la gestión de usuarios.' },
  VENTAS: { nombre: 'Atención y ventas', descripcion: 'Productos, categorías, pedidos y confirmación de pagos.' },
  ALMACEN: { nombre: 'Almacén', descripcion: 'Productos y stock; consulta de pedidos.' },
  OPERARIO: { nombre: 'Operario de taller', descripcion: 'Consulta pedidos pagados y registra el avance del trabajo.' },
};

/** Qué secciones del panel ve cada rol (el backend valida lo mismo). */
export const PERMISOS = {
  productos: ['ADMIN', 'VENTAS', 'ALMACEN'] as Rol[],
  categorias: ['ADMIN', 'VENTAS', 'ALMACEN'] as Rol[],
  pedidos: ['ADMIN', 'VENTAS', 'ALMACEN', 'OPERARIO'] as Rol[],
  usuarios: ['ADMIN'] as Rol[],
  recibos: ['ADMIN', 'VENTAS'] as Rol[],
  eliminar: ['ADMIN'] as Rol[],
};

/** Estados que puede asignar un operario. */
export const ESTADOS_OPERARIO: EstadoPedido[] = ['EN_PREPARACION', 'ENVIADO', 'ENTREGADO'];

export interface Usuario {
  id: number;
  email: string;
  nombre: string;
  rol: Rol;
  activo: boolean;
}

export const TIPOS: { valor: TipoProducto; nombre: string; descripcion: string }[] = [
  { valor: 'PLANCHA', nombre: 'Planchas', descripcion: 'Melamina de 15 y 18 mm en colores lisos y maderados.' },
  { valor: 'SERVICIO', nombre: 'Servicios', descripcion: 'Optimización del despiece, corte, canteado e instalación en obra.' },
];

export const ESTADOS: Record<EstadoPedido, string> = {
  PENDIENTE_PAGO: 'Pendiente de pago',
  PAGADO: 'Pagado',
  EN_PREPARACION: 'En preparación',
  ENVIADO: 'Enviado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

export const METODOS_PAGO: Record<MetodoPago, string> = {
  TARJETA: 'Tarjeta o Yape (en línea)',
  YAPE_PLIN: 'Plin (con captura)',
  TRANSFERENCIA: 'Transferencia bancaria',
};

/** Explicación corta que se muestra al elegir la forma de pago. */
export const METODOS_PAGO_DETALLE: Record<MetodoPago, string> = {
  TARJETA: 'Pagas al instante en la ventana segura de Culqi, con tarjeta de crédito, débito o con Yape.',
  YAPE_PLIN: 'Plineas al número de la tienda y nos envías la captura por WhatsApp.',
  TRANSFERENCIA: 'Depositas en nuestra cuenta y nos envías la constancia.',
};

export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string | null;
  tipo: TipoProducto;
}

export interface Producto {
  id: number;
  nombre: string;
  sku: string | null;
  descripcion: string | null;
  precio: number;
  precioOferta: number | null;
  precioVigente: number;
  stock: number;
  tipo: TipoProducto;
  categoriaId: number | null;
  categoriaNombre: string | null;
  medidas: string | null;
  color: string | null;
  espesor: string | null;
  imagenUrl: string | null;
  activo: boolean;
  destacado: boolean;
  soloCotizacion: boolean;
}

export type ProductoRequest = Omit<Producto, 'id' | 'precioVigente' | 'categoriaNombre'>;

export interface Pagina<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ItemPedido {
  productoId: number;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}

export interface Pedido {
  id: number;
  codigo: string;
  nombreCliente: string;
  email: string;
  telefono: string;
  documento: string;
  direccion: string;
  distrito: string;
  notas: string | null;
  subtotal: number;
  igv: number;
  total: number;
  estado: EstadoPedido;
  metodoPago: MetodoPago;
  referenciaPago: string | null;
  fechaCreacion: string;
  items: ItemPedido[];
  reciboId: number | null;
  reciboNumero: string | null;
  reciboEstado: 'EMITIDO' | 'ANULADO' | null;
}

export interface Recibo {
  id: number;
  numero: string;
  estado: 'EMITIDO' | 'ANULADO';
  fechaEmision: string;
  fechaAnulacion: string | null;
  pedidoCodigo: string;
  emisorRazonSocial: string;
  emisorRuc: string;
  emisorDireccion: string | null;
  emisorTelefono: string | null;
  clienteNombre: string;
  clienteDocumentoTipo: 'DNI' | 'RUC';
  clienteDocumento: string;
  clienteDireccion: string | null;
  clienteEmail: string | null;
  subtotal: number;
  igv: number;
  total: number;
  metodoPago: MetodoPago;
  referenciaPago: string | null;
  items: ItemPedido[];
}

export interface PedidoRequest {
  nombreCliente: string;
  email: string;
  telefono: string;
  documento: string;
  direccion: string;
  distrito: string;
  notas: string;
  metodoPago: MetodoPago;
  items: { productoId: number; cantidad: number }[];
}

export interface FiltrosProducto {
  tipo?: TipoProducto | null;
  categoriaId?: number | null;
  q?: string | null;
  destacados?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

/** Extrae el mensaje legible de un error del backend (ProblemDetail). */
export function mensajeError(err: any, porDefecto = 'No se pudo completar la operación'): string {
  const body = err?.error;
  if (body?.errores) {
    const primero = Object.entries(body.errores)[0];
    if (primero) return `${primero[0]}: ${primero[1]}`;
  }
  if (err?.status === 0) return 'No hay conexión con el servidor. Revisa que el backend esté encendido.';
  if (err?.status === 403) return body?.detail ?? 'Tu rol no tiene permiso para esta acción.';
  return body?.detail ?? porDefecto;
}

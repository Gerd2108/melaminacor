# Melamincorp — tienda en línea (Angular + Spring Boot + PostgreSQL)

Sistema web de MelaminCorp (Ate Vitarte, Lima): venta de **planchas de melamina** y sus **servicios** (optimización del despiece con Lepton Optimizer, corte a medida, canteado e instalación en obra), con:

- Catálogo público con filtros (línea, categoría, búsqueda, orden) y paginación.
- Carrito persistente, checkout y pagos con **tarjeta (Culqi)**, **Yape/Plin** o **transferencia**.
- Botón de **cotización por WhatsApp** en cada producto y en el carrito (los servicios se cotizan según el proyecto).
- **Panel de administración** con login JWT: productos (con subida de imágenes), categorías y pedidos.
- Precios en soles con **IGV incluido**; el pedido guarda op. gravada e IGV por separado para la boleta/factura.

## Requisitos

| Herramienta | Versión |
|---|---|
| Java | 21 |
| Maven | 3.9+ (o el que trae tu IDE) |
| Node.js | 20 o 22 |
| PostgreSQL | 14+ (o Docker) |

## 1. Base de datos

Con Docker (lo más rápido):

```bash
cd backend
docker compose up -d
```

Sin Docker, crea la base y el usuario en tu PostgreSQL:

```sql
CREATE DATABASE melamina;
CREATE USER melamina WITH PASSWORD 'melamina';
GRANT ALL PRIVILEGES ON DATABASE melamina TO melamina;
ALTER DATABASE melamina OWNER TO melamina;
```

Las tablas se crean solas al arrancar el backend (`ddl-auto: update`).

## 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Arranca en `http://localhost:8080`. La primera vez crea:

- El usuario admin: **admin@melamincorp.pe / Admin123!** (cámbialo con las variables `ADMIN_EMAIL` y `ADMIN_PASSWORD`).
- 6 categorías iniciales (Melamina 18 mm, Melamina 15 mm, Optimización y despiece, Corte a medida, Canteado, Instalación) y 15 productos de muestra con SKU `DEMO-`.

Variables de entorno importantes (todas tienen valor por defecto para desarrollo):

| Variable | Para qué sirve |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `JWT_SECRET` | Clave del token (Base64, 32+ bytes). **Cámbiala en producción**: `openssl rand -base64 48` |
| `CORS_ORIGINS` | Dominios del frontend, separados por coma |
| `CULQI_SECRET_KEY` | Llave secreta de Culqi (`sk_test_...` / `sk_live_...`) |
| `UPLOADS_DIR` | Carpeta donde se guardan las imágenes |

## 3. Frontend

```bash
cd frontend
npm install
npm start
```

Abre `http://localhost:4200`. El panel está en `http://localhost:4200/admin`.

Edita `src/environments/environment.development.ts` (y `environment.ts` para producción) con:
el nombre de tu empresa, tu número de WhatsApp, tus datos de Yape/Plin y cuenta bancaria, y la llave pública de Culqi.

## 4. Primeros pasos en el panel

1. Entra a `/admin` con el usuario admin.
2. Revisa las **Categorías** y ajústalas a lo que vendes.
3. Crea tus **Productos**: elige la línea, sube la foto, marca "Mostrar en el inicio" para los destacados.
   Para servicios marca "Solo por cotización": se muestran con precio "Desde" y se cotizan por WhatsApp. Un precio 0 se muestra como "Sin costo con tu compra".
4. Los pedidos con Yape o transferencia llegan como *Pendiente de pago*: cuando verifiques el abono,
   ábrelo en **Pedidos**, pon el N° de operación y cámbialo a *Pagado* (eso descuenta el stock).

## Roles y permisos

Basados en los actores del análisis de MelaminCorp. El backend valida cada permiso (no solo se ocultan botones).

| Acción | Administrador | Atención y ventas | Almacén | Operario de taller |
|---|:-:|:-:|:-:|:-:|
| Ver y editar productos y categorías | Sí | Sí | Sí | No |
| Eliminar productos o categorías | Sí | No | No | No |
| Ver pedidos | Sí | Sí | Sí | Sí |
| Confirmar pagos y cancelar pedidos | Sí | Sí | No | No |
| Marcar avance (en preparación, listo, entregado) | Sí | Sí | No | Sí, solo pedidos pagados |
| Crear usuarios, cambiar roles, desactivar | Sí | No | No | No |
| Cambiar su propia contraseña | Sí | Sí | Sí | Sí |

Con `app.demo.cargar: true` se crean usuarios de prueba (contraseña `Melamin123!`):
`ventas@melamincorp.pe`, `almacen@melamincorp.pe` y `taller@melamincorp.pe`.
Desactívalos desde **Usuarios** antes de usar el sistema en producción.

## Estructura

```
backend/src/main/java/pe/melamina/tienda/
├── config/        Seguridad, CORS, imágenes estáticas, datos iniciales
├── security/      Generación y validación de JWT
├── entity/        Producto, Categoria, Pedido, PedidoItem, Usuario + enums
├── repository/    Spring Data JPA + filtros dinámicos (Specifications)
├── dto/           Records de entrada/salida con validaciones
├── service/       Reglas de negocio: stock, IGV, pagos Culqi, imágenes
├── controller/    API REST pública (/api/public) y de admin (/api/admin)
└── exception/     Errores en formato ProblemDetail

frontend/src/app/
├── core/          Modelos, servicios HTTP, carrito, auth, interceptor, guard
├── shared/        Tarjeta de producto y pipes (soles, imagen)
├── layout/        Plantilla pública y plantilla del admin
├── pages/         Inicio, catálogo, detalle, carrito, checkout, confirmación
└── admin/         Login, productos, formulario de producto, categorías, pedidos
```

## API

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/public/productos?tipo=&categoriaId=&q=&destacados=&page=&size=&sort=` | Público |
| GET | `/api/public/productos/{id}` | Público |
| GET | `/api/public/categorias` | Público |
| POST | `/api/public/pedidos` | Público |
| GET | `/api/public/pedidos/{codigo}?email=` | Público (requiere el correo del pedido) |
| POST | `/api/public/pagos/culqi` | Público |
| POST | `/api/auth/login` | Público |
| GET/POST/PUT/DELETE | `/api/admin/productos` | Admin |
| POST/PUT/DELETE | `/api/admin/categorias` | Admin |
| GET, PATCH `/{id}/estado` | `/api/admin/pedidos` | Admin |
| POST (multipart, campo `archivo`) | `/api/admin/uploads` | Admin |

## Reglas de negocio incluidas

- El precio siempre se toma de la base de datos al crear el pedido, nunca del navegador.
- El stock se valida al crear el pedido y se **descuenta cuando pasa a Pagado**; si se cancela, se repone.
- Antes de cobrar con tarjeta se vuelve a verificar el stock.
- Un producto con pedidos no se puede eliminar (se sugiere desactivarlo).

## Pagos con Culqi

1. Crea tu cuenta en Culqi y copia las llaves de prueba (`pk_test_...` y `sk_test_...`).
2. La pública va en `environment.ts` del frontend; la secreta en `CULQI_SECRET_KEY` del backend.
3. Prueba con las tarjetas de prueba de su documentación.
4. Antes de producción revisa en `docs.culqi.com` si hay cambios en el Checkout o en la API de cargos,
   y considera activar 3D Secure y webhooks.

## Para producción

- Cambia `ddl-auto` a `validate` y usa **Flyway** para migraciones.
- Usa secretos reales (`JWT_SECRET`, contraseña del admin, llaves de Culqi) por variables de entorno.
- Guarda las imágenes en un servicio externo (S3, Cloudinary) cambiando solo `AlmacenamientoService`.
- Compila el frontend con `npm run build` y sírvelo con Nginx o un hosting estático.

## Ideas para seguir

Costo de envío por distrito, correos de confirmación (Spring Mail), comprobantes electrónicos SUNAT
(vía un OSE/PSE como Nubefact), cotizador de cortes para tableros, galería de varias fotos por producto
y SEO con Angular SSR.

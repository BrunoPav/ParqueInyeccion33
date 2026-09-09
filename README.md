# Taller Mecánico — Backend

API REST para el historial de un taller mecánico: clientes, sus vehículos, y los servicios
(intervenciones) realizados sobre cada vehículo.

**En producción:** https://parqueinyeccion33.onrender.com
*(Plan gratuito: la primera request tras un rato de inactividad puede tardar unos segundos en despertar
la instancia.)*

## Stack

- **Java 17** + **Spring Boot 4.1** (Web, Data JPA, Validation)
- **PostgreSQL 18** — mismo motor y versión en desarrollo (Docker) y producción (Neon)
- **Docker** multi-etapa para build y despliegue
- **JUnit 5 + Mockito** para tests de unidad, **`@DataJpaTest`** para tests de repository
- Despliegue: **Render** (aplicación) + **Neon** (base de datos), misma región (`us-east-2`)

## Modelo de datos

```
Cliente 1──N Vehiculo 1──N Servicio
```

| Entidad | Campos | Notas |
|---|---|---|
| **Cliente** | `id`, `nombre`, `contacto`, `activo` | `activo` controla el **borrado lógico**: el sistema es un historial, borrar un cliente destruiría la trazabilidad de qué auto trajo quién |
| **Vehiculo** | `id`, `marca`, `modelo`, `anio`, `patente` (única), `kilometraje`, `cliente_id` | Su visibilidad se **deriva** del `activo` de su dueño — no tiene flag propio |
| **Servicio** | `id`, `fecha`, `descripcion`, `precio` (`BigDecimal`), `vehiculo_id` | `precio` es lo que se cobra al cliente, nunca `Double` |

Las claves primarias son `Long` con `GenerationType.SEQUENCE` (`allocationSize = 50`): en Postgres eso
habilita batch inserts, porque Hibernate reserva un bloque de ids sin viajar a la base en cada insert.

## Arquitectura

**Package by feature**: cada entidad tiene su propia carpeta con entidad, repository, service, DTO,
mapper y controller adentro, en vez de agrupar por capa a nivel raíz.

```
cliente/    vehiculo/    servicio/    common/
```

`common/` es lo único transversal: las excepciones de dominio y el manejador global de errores.

**Reglas que se sostienen en las tres features:**

- El **Service expone solo DTOs** — la entidad nunca sale de Service + Repository.
- Mapeo entidad ↔ DTO **manual**, sin MapStruct: con pocas entidades y campos, es proporcional.
- El filtro por `activo` vive **en la firma del método derivado** de Spring Data
  (`findByCliente_ActivoTrue`), nunca en un `findAll()` filtrado a mano.
- Filtrar una colección por el id de su padre devuelve **404 si el padre no existe**, `200` con `[]` si
  existe pero no tiene hijos.

## Endpoints

Formato de error uniforme en 400/404/409: `{ "status", "mensaje", "timestamp" }`.

### Clientes

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/clientes` | Lista clientes activos. `?nombre=` filtra por nombre, `?activo=false` incluye inactivos |
| `GET` | `/api/clientes/{id}` | Busca por id — `404` si no existe |
| `POST` | `/api/clientes` | Crea un cliente (siempre activo) — `201` + `Location` |
| `PUT` | `/api/clientes/{id}` | Reemplaza nombre y contacto |
| `PATCH` | `/api/clientes/{id}` | Activa/desactiva (`{"activo": true\|false}`) — idempotente, sin `DELETE` |

### Vehículos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/vehiculos` | Lista vehículos de clientes activos. `?clienteId=` filtra por dueño (`404` si el cliente no existe) |
| `GET` | `/api/vehiculos/{id}` | Busca por id |
| `GET` | `/api/vehiculos?patente=` | Busca por patente (única) |
| `POST` | `/api/vehiculos` | Crea un vehículo — `409` si la patente ya existe |
| `PUT` | `/api/vehiculos/{id}` | Reemplaza el vehículo |
| `DELETE` | `/api/vehiculos/{id}` | Borrado físico — `204` |

### Servicios

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/servicios` | Lista servicios de vehículos visibles. `?vehiculoId=` filtra por vehículo, ordenados por fecha |
| `GET` | `/api/servicios/{id}` | Busca por id |
| `POST` | `/api/servicios` | Registra un servicio — `201` + `Location` |
| `PUT` | `/api/servicios/{id}` | Reemplaza el servicio |
| `DELETE` | `/api/servicios/{id}` | Borrado físico — `204` |

## Decisiones técnicas destacadas

| Decisión | Por qué |
|---|---|
| Borrado lógico solo en `Cliente` | `Vehiculo` y `Servicio` sí tienen `DELETE` físico: ahí el borrado no destruye trazabilidad de negocio |
| Activar/desactivar por `PATCH`, no `DELETE` | `DELETE` no puede expresar la reactivación, y el recurso sigue existiendo |
| DTOs de entrada con wrappers (`Boolean`, `Integer`), nunca primitivos | un primitivo no distingue "campo ausente" de `false`/`0`: Jackson rechaza el request entero con un 400 sin decir qué campo falló |
| `open-in-view=false` | el Service ya expone solo DTOs; ninguna asociación LAZY debería llegar a la capa web |
| `Dockerfile` multi-etapa (Maven+JDK → JRE) | la imagen final no lleva compilador ni código fuente: ~250 MB en vez de ~1 GB |
| Configuración por variables de entorno | ninguna credencial de producción vive en el repositorio |

El resto de las decisiones (y su porqué completo) está documentado en `CLAUDE.md`.

## Correr el proyecto localmente

```bash
docker compose up -d          # levanta Postgres + la app
./mvnw test                   # corre los tests (usa H2, no depende de Docker)
```

La app queda disponible en `http://localhost:8080`.

# Contexto del proyecto: Taller Mecánico Backend

## Descripción general

Sistema de historial para un taller mecánico.

## Alcance inicial

- Gestión de clientes
- Gestión de vehículos asociados a clientes
- Registro de servicios/intervenciones por vehículo

Diseño pensado para ser extensible: en el futuro se podrían sumar turnos, presupuestos o facturación sin
romper el modelo actual.

## Stack tecnológico

- **Backend**: Java 17 + Spring Boot 4.1 (package by feature, con capas dentro de cada feature)
- **Frontend**: Flutter + Riverpod (arquitectura limpia)
- **Base de datos**: PostgreSQL 17 en Docker (`docker-compose.yml`), mismo motor en desarrollo y producción
- **Herramienta de inspección**: pgAdmin en `localhost:5050`, servicio con profile `tools`
- **Despliegue**: Docker + nube (proveedor a definir)

---

## Modelo de datos

Los tipos son los que genera Hibernate a partir de las anotaciones JPA — el esquema no se escribe a mano.

### Tabla `clientes` (implementada)

| Columna | Tipo | Detalle |
|---|---|---|
| id | BIGINT | PK, `GenerationType.SEQUENCE` (`allocationSize = 50`) |
| nombre | VARCHAR(100) | NOT NULL |
| contacto | VARCHAR(100) | Teléfono o email |

### Tabla `vehiculos` (planificada)

| Columna | Tipo | Detalle |
|---|---|---|
| id | BIGINT | PK, `GenerationType.SEQUENCE` (`allocationSize = 50`) |
| marca | VARCHAR(50) | NOT NULL |
| modelo | VARCHAR(50) | NOT NULL |
| anio | INTEGER | NOT NULL. Rango válido vía Bean Validation, no vía `length` |
| patente | VARCHAR(10) | NOT NULL, UNIQUE |
| kilometraje | INTEGER | NOT NULL, default `0` en Java |
| cliente_id | BIGINT | FK → clientes.id, NOT NULL |

Relación con `Cliente`: **unidireccional** (`@ManyToOne(fetch = LAZY)` en `Vehiculo`; `Cliente` no tiene
la colección). Los vehículos de un cliente se obtienen con una query del repository, no navegando una
colección en memoria.

### Tabla `servicios` (planificada)

| Columna | Tipo | Detalle |
|---|---|---|
| id | BIGINT | PK, `GenerationType.SEQUENCE` (`allocationSize = 50`) |
| fecha | DATE | NOT NULL |
| descripcion | TEXT | NOT NULL |
| precio | DECIMAL(10,2) | `BigDecimal` en Java — nunca `Double`. Es lo que se le **cobra al cliente**; el costo interno del taller sería otro campo |
| vehiculo_id | BIGINT | FK → vehiculos.id |

**Relaciones**: cliente 1→N vehículos, vehículo 1→N servicios.

> `patente` es UNIQUE y `nombre` no: eso determina el tipo de retorno de cada búsqueda
> (`Optional<T>` vs. `List<T>`).

---

## Arquitectura

Organización **package by feature**: una carpeta por entidad con sus responsabilidades adentro, en vez de
agrupar por capa (`controller/`, `service/`, `repository/`…) a nivel raíz.

```
com.User.taller_mecanico_backend/
├── TallerMecanicoBackendApplication.java
├── cliente/
│   ├── Cliente.java              → @Entity mapeada a la tabla
│   ├── ClienteRepository.java    → interfaz Spring Data, acceso a datos
│   ├── ClienteService.java       → lógica de negocio; expone solo DTOs
│   ├── ClienteDTO.java           → record de transferencia
│   ├── ClienteMapper.java        → @Component, conversión manual entidad ↔ DTO
│   └── ClienteController.java    → endpoints REST
├── vehiculo/                     → mismo patrón (pendiente)
├── servicio/                     → mismo patrón (pendiente)
└── common/                       → transversal a todas las features
    ├── RecursoNoEncontradoException.java
    ├── ErrorRespuesta.java
    └── ManejadorDeExcepciones.java
```

Package by feature **no significa cero paquetes compartidos**: lo transversal (cross-cutting concerns,
como el manejo de errores) vive en `common/`, porque no pertenece a ninguna feature en particular.

---

## Convenciones

**Nomenclatura**

- Todo en español: clases, métodos, campos, mensajes de error, commits y documentación.
- Los términos técnicos sin traducción natural quedan en inglés (`Repository`, `record`).

**API REST**

- Recursos bajo `/api/<recurso>` en plural: `/api/clientes`.
- Filtrar una colección es un **query param**, no una ruta nueva: `/api/clientes?nombre=Juan`.
  Una ruta como `/api/clientes/buscar` sería estilo RPC.
- Una colección vacía devuelve **200 con `[]`**, nunca 404. El 404 es para un recurso identificado que
  no existe.
- Status: `200` al listar/obtener, `201` + header `Location` al crear, `204` al borrar,
  `404` vía el handler global.

**Capas**

- El **Service expone solo DTOs**. La entidad no sale nunca de Service + Repository; el Controller no la ve.
- El Service habla en lenguaje de dominio y **no importa nada de `org.springframework.http`**:
  lanza `RecursoNoEncontradoException`, no decide status HTTP.
- Los controllers devuelven el DTO pelado. Usar `ResponseEntity` **solo** cuando el status varía según el
  resultado o hace falta un header (ej. `Location` en el POST).
- **Usar siempre el valor de retorno de `save()`**, nunca el objeto que se le pasó.

**Otros**

- Imports `jakarta.*`, no `javax.*` (Spring Boot 3+).
- Inyección **por constructor**, con el campo `final`. Sin `@Autowired` en campos.

---

## Decisiones vigentes

| Decisión | Por qué |
|---|---|
| Package by feature, no por capa | todo lo de una entidad junto; escala mejor al crecer las features |
| Cross-cutting concerns en `common/` | el manejo de errores no pertenece a ninguna feature |
| PK `Long` + `GenerationType.SEQUENCE` (`allocationSize = 50`) | en Postgres permite batch inserts: Hibernate reserva un bloque de ids y no necesita un viaje por fila. Costo aceptado: huecos en los ids, irrelevante en una clave subrogada |
| `JpaRepository`, no `CrudRepository` | el proyecto ya está comprometido con JPA; la portabilidad sería teórica |
| Inyección por constructor | dependencias explícitas, campo `final`, test sin levantar Spring |
| DTO como `record`, uno solo por entidad | cero boilerplate y cero dependencias; separar request/response sería YAGNI hoy |
| El Service expone DTOs, no entidades | límite de encapsulamiento más fuerte que convertir en el Controller |
| Mapeo manual en `ClienteMapper`, no MapStruct | con pocas entidades y pocos campos el mapeo a mano es proporcional y explícito |
| Excepción de dominio unchecked + `@RestControllerAdvice` | la traducción a 404 vive en un solo lugar; unchecked además dispara el rollback de `@Transactional` |
| Validación con Bean Validation en el DTO | evita duplicar la misma regla en el Service (pendiente de implementar) |
| PostgreSQL en Docker desde el arranque | paridad dev/prod; el dialecto lo autodetecta Hibernate. H2 se usó solo como paso intermedio para aislar variables (verificar el código antes de sumar infraestructura) |
| `ddl-auto=update` solo en desarrollo | en producción van migraciones versionadas (Flyway/Liquibase) |
| `spring.jpa.open-in-view=false` | el Service expone solo DTOs, así que ninguna asociación perezosa llega a la capa web: OSIV no aporta nada y enmascararía problemas de N+1 |
| Batching activo (`batch_size=25`, `order_inserts`, `order_updates`) | `SEQUENCE` **habilita** el batching pero no lo enciende; sin estas properties el beneficio sería solo potencial |
| `VehiculoDTO` plano con `clienteId`, no un `ClienteDTO` anidado | simetría entrada/salida (el POST solo necesita el id) y cero problemas de lazy: `getCliente().getId()` no dispara la carga. Se separará request/response solo si el frontend pide el nombre del dueño en un listado |
| Mapper **puro**: recibe la entidad relacionada ya resuelta | inyectarle el repository lo obligaría a hacer I/O y a decidir una regla de negocio. El mapper convierte, el Service decide |
| `RecursoExistente` → `409 Conflict` | una patente duplicada es un error **del cliente**, no del servidor: sin manejo explícito la violación de constraint saldría como 500 |
| **Borrado lógico** en `Cliente` (`activo`), no `DELETE` físico | el sistema es un **historial**: borrar un cliente destruiría la trazabilidad de qué auto trajo quién. `Cliente` **no tiene endpoint DELETE**; `Vehiculo` y `Servicio` sí, porque ahí el borrado físico sigue teniendo sentido |
| El PATCH de estado es **idempotente** | aplicar dos veces "desactivar" no es un error: si la respuesta se pierde y el frontend reintenta, tiene que funcionar igual. `PUT`, `PATCH` y `DELETE` deben serlo |
| `EstadoClienteDTO(boolean activo)` como cuerpo del PATCH | con un `ClienteDTO` completo el contrato quedaría ambiguo (¿se aplica el `nombre` que mandaron?). Un record dedicado hace que **la petición ambigua no se pueda expresar** |
| Activar/desactivar vía **`PATCH`**, no `DELETE` | `DELETE` mentiría (el recurso sigue existiendo) y no puede expresar la **reactivación**; con `PATCH` activar y desactivar son la misma operación con distinto valor |
| La visibilidad de un `Vehiculo` se **deriva** del `activo` de su cliente | un flag propio en `Vehiculo` no tendría uso real hoy, y **cascadear el flag destruiría información**: al reactivar no se podría distinguir "inactivo por su dueño" de "inactivo por sí mismo". Se agregará si aparece el caso de dar de baja un vehículo con el cliente activo |

**Decisiones abiertas**

- Ninguna pendiente.

---

## Estado del proyecto

La aplicación corre sobre PostgreSQL en Docker. Los 6 endpoints de `cliente` están verificados uno por uno.

| Paquete | Archivos | Estado |
|---|---|---|
| `cliente/` | entidad, repository, service, DTO, mapper, controller | CRUD REST completo y verificado |
| `common/` | `RecursoNoEncontradoException` (404), `RecursoExistente` (409), `ErrorRespuesta`, `@RestControllerAdvice` | hecho |
| `vehiculo/` | entidad, repository, service, DTO, mapper, controller | CRUD REST completo y verificado |
| `servicio/` | entidad, repository, service, DTO, mapper, controller | CRUD REST completo y verificado |

**El modelo de datos está cerrado**: las tres entidades y las dos relaciones
(`Cliente` 1→N `Vehiculo` 1→N `Servicio`) están implementadas y probadas endpoint por endpoint.

Rama de trabajo actual: `feature_servicio`.

**Convenciones que aplican a las tres features**

- Filtrar una colección por el id de su padre devuelve **404 si el padre no existe**, y `200` con `[]`
  si el padre existe pero no tiene hijos.
- **Visibilidad y borrado lógico:** los **listados generales** (`/api/vehiculos`, `/api/servicios`)
  excluyen lo que cuelga de un cliente inactivo; las **búsquedas por id** y los **listados por padre
  explícito** (`?clienteId=`, `?vehiculoId=`) devuelven igual. El criterio: si no pediste nada en
  particular, se muestra lo relevante; si pediste algo puntual, se entrega.
- El filtro por `activo` viaja **en la firma del método derivado** (`findByActivo`,
  `findByCliente_ActivoTrue`), nunca en un `findAll()` que después se filtre a mano: así es imposible
  olvidarlo. **No queda ningún `findAll()` en el proyecto.**
- El mapper navega la relación (`entidad.getCliente().getId()`); las entidades **no** exponen helpers
  del tipo `getClienteId()`: cada entidad expone solo su propio estado.

**Defectos conocidos**

- Ninguno pendiente.

## Próximos pasos

1. **Agregar validación**: `spring-boot-starter-validation` al `pom.xml` (⚠️ **no viene incluido** en el
   starter web desde Spring Boot 2.3 — sin la dependencia las anotaciones se ignoran en silencio),
   `@NotBlank`/`@Size` en los DTOs, `@Valid` en los Controllers, y un `@ExceptionHandler` para
   `MethodArgumentNotValidException` → 400. Incluye el rango de `anio` en `VehiculoDTO` (`@Min`/`@Max`).
2. **Tests de los services** (la inyección por constructor lo hace trivial).

## Orden de construcción del proyecto

1. ~~Modelo de datos y base de datos~~ (hecho)
2. Backend con Spring Boot — **en curso**
3. ~~Migrar a PostgreSQL con Docker Compose~~ (hecho)
4. Frontend en Flutter consumiendo la API vía endpoints REST
5. Dockerizar la aplicación y desplegar
6. Documentar en paralelo el uso de IA en cada etapa

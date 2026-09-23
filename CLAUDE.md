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
- **Base de datos**: PostgreSQL 18 en Docker (`docker-compose.yml`), mismo motor y versión en desarrollo y producción (Neon)
- **Herramienta de inspección**: pgAdmin en `localhost:5050`, servicio con profile `tools`
- **Despliegue**: Docker + Render (app) + Neon (PostgreSQL) — https://parqueinyeccion33.onrender.com

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
| Validación con Bean Validation en el DTO | evita duplicar la misma regla en el Service. **No reemplaza** las restricciones de la base: la base garantiza, la validación comunica temprano y con un mensaje claro |
| Formato de error propio (`ErrorRespuesta`) y no `ProblemDetail` | consistencia entre los tres códigos (400, 404, 409) con lo ya construido. El estándar sería **RFC 7807** (`application/problem+json`), que Spring soporta con `ProblemDetail`; se usaría si la API fuera pública o de cara a terceros |
| Mensajes de validación cortos, sin repetir el campo | el handler ya antepone `campo + ": "`, así que un mensaje que repita el nombre queda redundante (`nombre: es obligatorio`, no `nombre: el nombre es obligatorio`) |
| PostgreSQL en Docker desde el arranque | paridad dev/prod; el dialecto lo autodetecta Hibernate. H2 se usó solo como paso intermedio para aislar variables (verificar el código antes de sumar infraestructura) |
| PostgreSQL **18** local y en la nube (no 17) | Neon (el proveedor elegido) provisionó el proyecto en 18; se subió la versión local en vez de forzar 17 en Neon, para no romper la paridad dev/prod. Sin impacto: el esquema no usa nada específico de versión y Hibernate autodetecta el dialecto igual |
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
| `Dockerfile` **multi-etapa** (build con Maven+JDK → runtime con JRE) | la imagen final no lleva compilador, Maven ni código fuente: ~250 MB en vez de ~1 GB y menos superficie de ataque. Costo: `Dockerfile` más largo |
| Copiar `pom.xml` y bajar dependencias **antes** de copiar `src/` | Docker invalida capas en cascada; con el orden inverso, cambiar una línea de Java re-descargaría todas las dependencias en cada build |
| Imagen `maven:` en vez del wrapper `mvnw` | evita el fallo por finales de línea CRLF al ejecutar `mvnw` dentro de Linux, y `maven-wrapper.jar` está en `.gitignore` |
| Base `eclipse-temurin:17-jre`, no Alpine ni distroless | Alpine usa `musl` (riesgo con librerías nativas) y distroless no trae shell para debuggear. Se cambiaría si el tamaño pasara a importar |
| Usuario **no-root** (`spring`) en la imagen | por defecto el contenedor corre como root; un usuario sin privilegios limita el daño ante una ejecución de código |
| `ENTRYPOINT` en forma **exec**, no shell | así la JVM es el PID 1 y recibe el `SIGTERM` de `docker stop`: con la forma shell la señal se la queda `/bin/sh` y el apagado ordenado de Spring nunca corre |
| Configuración por **variables de entorno** con default de desarrollo (`${VAR:default}`) | ninguna credencial de producción vive en el repositorio, y la app corre sin configurar nada en local. Alternativa descartada: perfiles de Spring por entorno, innecesario con tan pocas properties |
| `server.port=${PORT:8080}` | los PaaS asignan el puerto por la variable `PORT` y no dejan elegirlo; sin esto la app quedaría inalcanzable al desplegar |
| Sin JAR en capas (`layertools`) | optimiza el redeploy incremental, pero suma complejidad para un problema que este proyecto todavía no tiene. YAGNI |
| **Los DTO de entrada usan wrappers (`Boolean`, `Integer`, `Long`), nunca primitivos** | un primitivo no puede representar "campo ausente": Jackson recibe `null`, no puede asignarlo y rechaza el request entero con un `400` genérico, sin decir qué campo falló. Con wrapper, un campo opcional se omite sin drama y uno obligatorio se marca con `@NotNull` y devuelve el mensaje claro del handler. Peor aún sería silenciar el error con `fail-on-null-for-primitives=false`: un `PATCH {}` se convertiría en `activo=false` y **desactivaría un cliente sin que nadie lo pidiera** |
| Formato del mensaje de validación: `campo:` + mensaje | los mensajes de los DTO ya empiezan con espacio (`" es obligatorio"`), así que el handler concatena con `":"` y no con `" "`. Con `" "` salía doble espacio (`nombre  es obligatorio`) |

| Proveedor de nube: **Render** (app) + **Neon** (base) | ninguno de los dos duerme la base a los 30 días como el Postgres gratuito de Render, apto para un link de portfolio. Costo: dos registros y dos dashboards en vez de uno. Ambos en la misma región (`us-east-2`/Ohio) para no pagar latencia de red entre app y base en cada query |

| **Spring Security + JWT**, no Basic Auth ni sesiones | es lo que piden los avisos de Spring y lo que corresponde a una API pública. Stateless: encaja con el deploy en Render y permite escalar sin compartir sesiones. Costo: más piezas (filtro propio, firma, expiración) que Basic |
| **Lecturas públicas, escrituras autenticadas** | si se cierra la API entera, un reclutador abre el link y no ve nada; si se deja abierta, cualquiera borra los datos. `GET` público resuelve las dos cosas |
| **Dos roles**: `DEMO` (público, escribe pero no borra) y `ADMIN` (privado, todo) | publicar las credenciales de `ADMIN` en el README dejaría la API tan abierta como antes, solo que con un paso extra. Con dos roles el visitante prueba el flujo completo sin poder destruir nada, y el enum `Rol` pasa a hacer algo real: se demuestra **autorización**, no solo autenticación |
| El constructor de `Usuario` **exige el rol**, sin default | con un default implícito, olvidarse del rol crearía un usuario con permisos que nadie pidió. Que el compilador lo exija hace que el descuido no se pueda escribir. `UsuarioService` crea con `DEMO` por mínimo privilegio |
| Contraseñas con **BCrypt** | hash de una sola vía (ni el sistema puede recuperarlas), con sal automática y lentitud deliberada que encarece la fuerza bruta. La columna es `VARCHAR(60)`: el largo exacto de un hash BCrypt |
| `UserDetailsService` como **adapter**, no `Usuario implements UserDetails` | la entidad JPA no importa tipos de Spring Security. Mismo criterio que "el Service expone DTOs": que una capa no se filtre en otra. Costo: una clase más |
| `Rol` como **enum** con `@Enumerated(EnumType.STRING)` | un `String` deja escribir `"JEFFE"` y falla en silencio; el enum hace que el error no compile. `STRING` y no el default `ORDINAL` porque este guarda la posición: insertar un valor al principio del enum cambiaría el significado de todas las filas existentes |
| El rol es un **campo**, no una subclase | un objeto no cambia de clase: ascender un empleado exigiría borrar la fila y crear otra. Mismo razonamiento por el que `activo` es un campo y no `ClienteActivo extends Cliente` |
| **Request y response separados** en `seguridad/` (`CredencialesDTO` / `TokenDTO` / `UsuarioDTORespuesta`) | única excepción a "un DTO por entidad": la contraseña es obligatoria en el request y **no puede salir nunca** en la respuesta. Con un DTO único, devolverla es expresable |
| El **login falla siempre igual** (401 genérico) | distinguir "usuario inexistente" de "contraseña incorrecta" permite enumerar usuarios válidos. Es el criterio opuesto al del resto de la API, donde los mensajes específicos son una virtud |
| **Usuario semilla** por variables de entorno, sin endpoint público de alta | un `POST /api/usuarios` abierto dejaría que cualquiera se cree un `ADMIN`; protegido, no habría forma de crear el primero. El sembrado es idempotente |
| CSRF desactivado | CSRF protege sesiones con cookie, que el navegador manda sola. El token va en una cabecera que pone el cliente: el ataque no aplica |
| `jjwt-gson` y no `jjwt-jackson` | Spring Boot 4 migró a Jackson 3 (`tools.jackson`) y `jjwt-jackson` todavía depende de Jackson 2, que ya no está en el classpath |

**Decisiones abiertas**

- El frontend todavía no tiene login: las escrituras desde la demo web devuelven 403 hasta implementarlo.

---

## Estado del proyecto

La aplicación corre sobre PostgreSQL en Docker, con borrado lógico y validación de entrada.
Todos los endpoints están verificados uno por uno con la aplicación corriendo.

| Paquete | Archivos | Estado |
|---|---|---|
| `cliente/` | entidad, repository, service, DTO, `EstadoClienteDTO`, mapper, controller | CRUD REST + borrado lógico, verificado |
| `common/` | `RecursoNoEncontradoException` (404), `RecursoExistente` (409), `ErrorRespuesta`, `@RestControllerAdvice` con los tres handlers (400/404/409) | hecho |
| `vehiculo/` | entidad, repository, service, DTO, mapper, controller | CRUD REST completo y verificado |
| `servicio/` | entidad, repository, service, DTO, mapper, controller | CRUD REST completo y verificado |

**El modelo de datos está cerrado**: las tres entidades y las dos relaciones
(`Cliente` 1→N `Vehiculo` 1→N `Servicio`) están implementadas y probadas endpoint por endpoint.

| tests | `ClienteServiceTest` (3), `VehiculoServiceTest` (2), `VehiculoRepositoryTest` (2, `@DataJpaTest`), `contextLoads` | 8 tests en verde, corren con H2 sin Docker |
| infraestructura | `Dockerfile` multi-etapa, `.dockerignore`, servicio `app` en `docker-compose.yml` | verificado: imagen construida (~250 MB), stack levantado y 9 endpoints probados dentro de Docker |
| `seguridad/` | `Usuario`, `Rol`, repository, service, `DetallesUsuarioService` (adapter), `JwtService`, `JwtFiltro`, `ConfiguracionSeguridad`, `AutenticacionController`, `SembradorUsuario`, DTOs | JWT funcionando, verificado con 8 casos (GET público 200, escritura sin token 403, login OK/fallido, token inventado 403) |
| despliegue | Render (app, plan free) + Neon (PostgreSQL 18, `us-east-2`) | **en producción**: https://parqueinyeccion33.onrender.com, verificado con 4 endpoints (200/404/201/400) contra la base real |

Rama de trabajo actual: `main`.

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

1. `README.md` para el repositorio: qué es, modelo de datos, tabla de endpoints, decisiones técnicas
   y el link al deploy.
2. Frontend en Flutter — etapa 4 del proyecto. El backend ya no depende de esto: tiene URL pública
   estable (https://parqueinyeccion33.onrender.com) para desarrollar contra ella en vez de `localhost`.

## Orden de construcción del proyecto

1. ~~Modelo de datos y base de datos~~ (hecho)
2. Backend con Spring Boot — **en curso**
3. ~~Migrar a PostgreSQL con Docker Compose~~ (hecho)
4. Frontend en Flutter consumiendo la API vía endpoints REST
5. Dockerizar la aplicación y desplegar
6. Documentar en paralelo el uso de IA en cada etapa

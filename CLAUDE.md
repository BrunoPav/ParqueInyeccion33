# Contexto del proyecto: Taller Mecánico Backend

## Descripción general
Sistema de historial para un taller mecánico.

## Alcance inicial
- Gestión de clientes
- Gestión de vehículos asociados a clientes
- Registro de servicios/intervenciones por vehículo

Diseño pensado para ser extensible: en el futuro se podrían sumar turnos, presupuestos o facturación sin romper el modelo actual.

## Stack tecnológico
- **Backend**: Java + Spring Boot (arquitectura en capas)
- **Frontend**: Flutter + Riverpod (arquitectura limpia)
- **Base de datos desarrollo**: SQLite
- **Base de datos producción**: PostgreSQL
- **Despliegue**: Docker + nube (a definir proveedor)

## Modelo de datos

### Tabla `clientes`
| Columna | Tipo | Detalle |
|---|---|---|
| id | INTEGER | PK, autoincremental |
| nombre | VARCHAR(100) | NOT NULL |
| contacto | VARCHAR(100) | Teléfono o email |

### Tabla `vehiculos`
| Columna | Tipo | Detalle |
|---|---|---|
| id | INTEGER | PK, autoincremental |
| marca | VARCHAR(50) | NOT NULL |
| modelo | VARCHAR(50) | NOT NULL |
| patente | VARCHAR(10) | NOT NULL, UNIQUE |
| kilometraje | INTEGER | DEFAULT 0 |
| cliente_id | INTEGER | FK → clientes.id |

### Tabla `servicios`
| Columna | Tipo | Detalle |
|---|---|---|
| id | INTEGER | PK, autoincremental |
| fecha | DATE | NOT NULL |
| descripcion | TEXT | NOT NULL |
| costo | DECIMAL(10,2) | |
| vehiculo_id | INTEGER | FK → vehiculos.id |

**Relaciones**: cliente 1→N vehículos, vehículo 1→N servicios.

## Arquitectura del backend (Spring Boot)
Estructura en 5 capas, cada una con responsabilidad única:

```
controller/   → recibe peticiones HTTP, expone endpoints REST
service/      → lógica de negocio y validaciones
repository/   → interfaces JPA, acceso a la base de datos
model/        → clases @Entity mapeadas a las tablas
dto/          → objetos de transferencia, no exponen las entidades directamente
```

## Orden de construcción del proyecto
1. Modelo de datos y base de datos (SQLite en desarrollo)
2. Backend con Spring Boot (arquitectura en capas)
3. Frontend en Flutter consumiendo la API vía endpoints REST
4. Dockerizar y desplegar (migrando a PostgreSQL)
5. Documentar en paralelo el uso de IA en cada etapa


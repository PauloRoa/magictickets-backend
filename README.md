# MagicTickets Backend

Microservicio backend de MagicTickets, plataforma de venta de tickets para eventos, expuesto vía API REST y respaldado por persistencia real en PostgreSQL. Este repositorio no es la entrega de un hito aislado: es la culminación del dominio Java construido y validado a lo largo de todo el curso — desde el modelo de negocio puro (Hito 1) hasta su integración full-stack real con el frontend (Hito 6).

El sistema expone la cartelera de eventos, permite crear eventos nuevos y ejecutar compras validando las reglas de negocio originales (cantidad positiva, máximo 5 tickets por compra, stock suficiente), documentado de forma automática con OpenAPI/Swagger, aislado por perfiles de entorno y con credenciales excluidas del control de versiones.

---

## Índice

1. [Recorrido del proyecto](#recorrido-del-proyecto)
2. [Pila tecnológica](#pila-tecnológica)
3. [Arquitectura](#arquitectura)
4. [Estructura del repositorio](#estructura-del-repositorio)
5. [Configuración de variables de entorno](#configuración-de-variables-de-entorno)
6. [Levantar el entorno](#levantar-el-entorno)
7. [Documentación y pruebas de contratos](#documentación-y-pruebas-de-contratos)
8. [API expuesta](#api-expuesta)
9. [Decisiones de diseño](#decisiones-de-diseño)
10. [Suite de tests](#suite-de-tests)
11. [Evidencia de cobertura](#evidencia-de-cobertura)

---

## Recorrido del proyecto

| Hito | Unidad | Qué aportó a este backend | Resultado |
|---|---|---|---|
| Hito 1 | Fundamentos de Calidad y TDD en Java | Dominio puro: `Event`, `TicketPurchaseService`, `PurchaseNotifier`, 3 excepciones de negocio. Reglas originales de compra (cantidad positiva, máximo 5, stock suficiente). 7 tests JUnit 5 + Mockito, 100% cobertura. | ✅ 10.0/10.0 |
| Hito 3 | Arquitectura Limpia y DDD | Reestructuración a Clean Architecture (`domain`/`application`/`infrastructure`). Incorporación de `EventDate` (Value Object), `ShowStatus`/`ShowCategory` (enums), patrón Repositorio (`EventRepository`), 2 excepciones nuevas (`InvalidEventDateException`, `EventNotFoundException`, backporteadas desde el dominio ampliado del frontend en Hito 2). 27 tests, 100% cobertura. | ✅ 10.0/10.0 |
| Hito 4 | Microservicios con Spring Boot, PostgreSQL y Docker | Conexión de la infraestructura a un stack productivo real: controladores REST, `GlobalExceptionHandler`, persistencia JPA contra PostgreSQL en Docker, Swagger/OpenAPI, aislamiento de perfiles `dev`/`prod`. 40 tests, 97% instructions / 100% branches. | ✅ 10.0/10.0 |
| Hito 6 | Integración Full-Stack y Validación | Conexión real de punta a punta con `magictickets-frontend`: CORS resuelto (`@CrossOrigin`), campo `imageUrl` incorporado al dominio, DTO `EventResponse` para alinear el contrato JSON con la UI, flujo de compra real conectado, credenciales excluidas del control de versiones (`.env`/`.gitignore`). 40 tests corregidos y verificados, 98% instructions / 100% branches. | ✅ 10.0/10.0 |

El dominio de negocio nunca se reinició entre hitos: cada uno construyó sobre las reglas y la arquitectura ya validadas por el anterior.

## Pila tecnológica

- Java 17
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA)
- PostgreSQL 18 (contenedor Docker)
- Hibernate ORM 7
- Springdoc OpenAPI 3.1.0 (Swagger-UI)
- JUnit 5 + Mockito + JaCoCo
- Maven

## Arquitectura

Clean Architecture en tres capas, establecida en Hito 3 y con la capa de infraestructura conectada a un stack productivo real desde Hito 4, integrada de punta a punta con el frontend desde Hito 6:

- **Domain (`domain/`):** entidad `Event` (incluye `imageUrl`, campo agregado en Hito 6 por decisión consciente de alinear el dominio al contrato del frontend), Value Object `EventDate`, enums (`ShowStatus`, `ShowCategory`), 5 excepciones de negocio y `PurchaseValidator`. Java puro — sin anotaciones de Spring ni JPA.
- **Application (`application/`):** casos de uso (`EventReadService`, `EventWriteService`, `TicketPurchaseService`) y el puerto `PurchaseNotifier`. Depende únicamente de contratos del dominio.
- **Infrastructure (`infrastructure/`):** controladores REST (con `@CrossOrigin` habilitado para `http://localhost:5173`), manejador global de excepciones, notificador concreto, DTOs de request/response y el adaptador JPA — el anillo que sí conoce Spring, Hibernate y PostgreSQL.

**Divergencia consciente respecto a Hito 3:** `PurchaseValidator` incorpora `@Component` para poder inyectarse vía IoC de Spring, algo que no necesitaba en Hito 3 (instanciado directamente en los tests). Es una tensión real entre "el dominio no conoce el framework" (regla de Hito 3) y "todo se resuelve por inyección de dependencias" (convención de Spring Boot) — se prioriza la segunda por ser el mecanismo estándar del framework, sin introducir ninguna dependencia de persistencia o web en la clase.

**Traducción de contrato JSON (`EventResponse`, Hito 6):** `EventController` no devuelve `Event` directamente, sino `EventResponse` (record en `infrastructure/controller/dto/`), que aplana `EventDate` a un `String` ISO plano y expone `imageUrl`. Esto resuelve que Jackson serializaba el Value Object `EventDate` como un objeto anidado (`{"value": "..."}`) en vez del string plano que espera el frontend, sin ensuciar el dominio con anotaciones de serialización — la traducción vive en el controlador, que es su responsabilidad en Clean Architecture.

## Estructura del repositorio

```
magictickets-backend/
├── pom.xml
├── docker-compose.yml
├── .env.example
├── README.md
└── src/
    ├── main/java/com/magictickets/backend/
    │   ├── domain/
    │   │   ├── entity/Event.java
    │   │   ├── enums/ShowStatus.java, ShowCategory.java
    │   │   ├── exception/ (5 excepciones de negocio)
    │   │   ├── repository/EventRepository.java
    │   │   ├── service/PurchaseValidator.java
    │   │   └── valueobject/EventDate.java
    │   │
    │   ├── application/
    │   │   ├── EventReadService.java, EventWriteService.java
    │   │   ├── port/PurchaseNotifier.java
    │   │   └── usecase/TicketPurchaseService.java
    │   │
    │   └── infrastructure/
    │       ├── controller/EventController.java, PurchaseController.java
    │       ├── controller/dto/EventRequest.java, EventResponse.java, PurchaseRequest.java
    │       ├── exception/GlobalExceptionHandler.java
    │       ├── notification/ConsolePurchaseNotifier.java
    │       └── persistence/EventEntity.java, EventJpaRepository.java,
    │           EventRepositoryJpaAdapter.java
    │
    └── test/java/com/magictickets/backend/ (espejo exacto de main)
```

## Configuración de variables de entorno

Las credenciales de la base de datos **no están hardcodeadas** en ningún archivo versionado (`docker-compose.yml`, `application-dev.yml`) — se inyectan en tiempo de ejecución mediante variables de entorno, siguiendo la pauta de seguridad de grado de producción de Hito 6.

**1. Copia el archivo de ejemplo:**

```bash
cp .env.example .env
```

`.env` está excluido en `.gitignore` y nunca debe subirse al repositorio; `.env.example` sí se versiona, documentando qué variables existen (con valores de desarrollo local, no de producción).

**2. Docker Compose lee `.env` automáticamente** (mismo directorio, comportamiento nativo) para inyectar `POSTGRES_USER`, `POSTGRES_PASSWORD` y `POSTGRES_DB` en el contenedor de PostgreSQL — no requiere ningún paso adicional.

**3. Spring Boot NO lee `.env` automáticamente.** Antes de levantar la aplicación o correr los tests, exporta manualmente `DB_USERNAME` y `DB_PASSWORD` en la misma sesión de terminal:

```powershell
# PowerShell (Windows)
$env:DB_USERNAME = "magic_user"
$env:DB_PASSWORD = "magic_password"
```

```bash
# bash/zsh (Linux/macOS)
export DB_USERNAME=magic_user
export DB_PASSWORD=magic_password
```

**Importante:** estas variables solo viven mientras esa ventana de terminal esté abierta — deben re-exportarse en cada sesión nueva antes de `./mvnw spring-boot:run` o `./mvnw clean test`.

## Levantar el entorno

**1. Base de datos:**

```bash
docker compose up -d
```

Inicia PostgreSQL 18 en un contenedor, expone el puerto `5432` y persiste los datos en un volumen nombrado.

**2. Aplicación** (con `DB_USERNAME`/`DB_PASSWORD` ya exportadas, ver sección anterior):

```bash
./mvnw spring-boot:run
```

Arranca bajo el perfil `dev` (activo por defecto) en `http://localhost:8080`, conectándose al contenedor levantado en el paso anterior.

**3. Ejecutar la suite de tests completa** (requiere Docker arriba y las mismas variables de entorno exportadas en la sesión):

```bash
./mvnw clean test
```

Regenera el reporte de cobertura JaCoCo en `target/site/jacoco/index.html`.

## Documentación y pruebas de contratos

- Swagger-UI: `http://localhost:8080/swagger-ui.html` — verificado funcional bajo el perfil `dev`.
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Ambos quedan deshabilitados bajo el perfil `prod` (`application-prod.yml`), verificado en Hito 4: acceder a cualquiera de las dos rutas bajo ese perfil retorna `404`.

## API expuesta

| Método | Ruta | Código de éxito | Códigos de error posibles |
|---|---|---|---|
| `GET` | `/api/v1/events` | 200 | — |
| `POST` | `/api/v1/events` | 201 | 400 (fecha inválida) |
| `POST` | `/api/v1/purchases` | 201 | 400, 404, 422 |

Ambos controladores tienen `@CrossOrigin(origins = "http://localhost:5173")` habilitado para permitir el consumo real desde `magictickets-frontend` sin bloqueo CORS. Contrato completo, con esquemas de request/response, disponible en Swagger-UI.

## Decisiones de diseño

**Manejo centralizado de errores (`GlobalExceptionHandler`):** cada una de las 5 excepciones de dominio se traduce a un código HTTP semántico:

| Excepción | Código | Motivo |
|---|---|---|
| `EventNotFoundException` | 404 | Recurso solicitado no existe |
| `InvalidQuantityException` | 400 | Dato de entrada inválido |
| `MaxTicketsExceededException` | 400 | Dato de entrada inválido |
| `InvalidEventDateException` | 400 | Dato de entrada inválido |
| `OutOfStockException` | 422 | Regla de negocio violada con sintaxis válida |

**Mapeo JPA de `EventDate`:** el Value Object `EventDate` (record, Hito 3) no puede mapearse como `@Embeddable` porque Hibernate no soporta records ahí (carecen de constructor vacío). Se optó por almacenar un `LocalDate` plano en `EventEntity`, reconstruyendo `EventDate` manualmente en `toDomain()`. Queda fuera de alcance (YAGNI) validar datos insertados directamente en la base de datos sin pasar por la aplicación.

**Alcance del flujo de compra:** `TicketPurchaseService` conserva intacta la lógica original de Hito 1 (incluyendo `PurchaseNotifier`, cuyo propósito fue justificar el uso de Mockito exigido en esa unidad), demostrando en ejecución real, desde Hito 4, las cuatro validaciones de negocio y sus códigos HTTP correspondientes.

**Validación duplicada entre frontend y backend (Hito 6), a propósito:** el frontend replica las reglas de cantidad/máximo/stock como feedback inmediato de UX; el backend las revalida siempre como única fuente de verdad real, ya que cualquier cliente puede saltarse la UI con una petición HTTP directa. Ninguna de las dos reemplaza a la otra.

**`imageUrl` en el dominio (Hito 6):** se agregó a `Event` por decisión consciente de ajustar el backend al contrato ya definido por el frontend, en vez de forzar al frontend a adaptarse. Aunque es un dato de naturaleza presentacional, se priorizó la coherencia end-to-end del ciclo de datos sobre la pureza estricta del dominio.

## Suite de tests

Framework: **JUnit 5** + **Mockito**. Patrón **AAA** en los 40 tests. Suite generada con asistencia de Claude Code desde Hito 4, revisada y corregida manualmente en cada hito (incluyendo la actualización de firmas tras agregar `imageUrl` en Hito 6).

| Clase de test | Tests | Qué cubre |
|---|---|---|
| `PurchaseValidatorTest` | 8 | Las tres reglas de validación originales de Hito 1, sin mocks |
| `EventTest` | 6 | Creación, categoría nula, estado inicial, reducción de stock |
| `EventDateTest` | 4 | Auto-validación del Value Object |
| `TicketPurchaseServiceTest` | 5 | Orquestación completa, con `EventRepository`/`PurchaseNotifier` mockeados |
| `EventReadServiceTest` | 2 | Delegación al repositorio |
| `EventWriteServiceTest` | 2 | Creación y persistencia de eventos |
| `EventControllerTest` | 2 | `@WebMvcTest`: códigos HTTP y JSON de `/api/v1/events` (contrato `EventResponse`) |
| `PurchaseControllerTest` | 2 | `@WebMvcTest`: creación exitosa y propagación de 404 |
| `GlobalExceptionHandlerTest` | 5 | Código HTTP correcto por cada excepción de dominio |
| `EventRepositoryJpaAdapterTest` | 3 | `@DataJpaTest` con H2: mapeo `Event ↔ EventEntity` |
| `MagicticketsBackendApplicationTests` | 1 | Arranque del contexto completo (requiere PostgreSQL activo y variables de entorno exportadas) |

**Nota sobre `EventRepositoryJpaAdapterTest`:** usa H2 en memoria (perfil `test`, `application-test.yml`) exclusivamente para poder ejecutar la capa de persistencia sin depender de que Docker esté corriendo. El resto de la suite corre bajo el perfil `dev`; `MagicticketsBackendApplicationTests` sí requiere el contenedor de PostgreSQL activo y `DB_USERNAME`/`DB_PASSWORD` exportadas, al conectarse contra la configuración real.

## Evidencia de cobertura

Reporte HTML en `target/site/jacoco/index.html`.

- **Instructions:** 98% (12 de 604 sin cubrir)
- **Branches:** 100% (0 de 16)

Cobertura por paquete: 100% en `domain` (todas sus subcarpetas), `application`, `infrastructure.controller`, `infrastructure.controller.dto` (incluye `EventResponse`, código nuevo de Hito 6), `infrastructure.exception` e `infrastructure.persistence`. Dos paquetes por debajo del 100%, con los mismos 12 missed instructions documentados desde el cierre de Hito 4 (97%, 12 de 534) — el total de instrucciones del proyecto creció a 604 con el código nuevo de esta unidad, y ese código nuevo quedó 100% cubierto, subiendo el porcentaje global a 98% sin agregar ninguna instrucción sin testear:

- `infrastructure.notification` (50%): `ConsolePurchaseNotifier` no tiene un test dedicado que verifique la línea de log — no se pidió explícitamente en el alcance de la suite.
- `com.magictickets.backend` (37%, paquete raíz): la clase de arranque `MagicticketsBackendApplication`, cuyo único método (`main`) no se ejecuta línea a línea en un test de contexto (`contextLoads`).

Ninguno de los dos vacíos corresponde a lógica de negocio ni a código nuevo de Hito 6; se documentan aquí por transparencia, no se completan artificialmente.

## JaCoCo

<img width="1514" height="504" alt="imagen" src="https://github.com/user-attachments/assets/694464b4-8273-410b-908b-80534e0a252f" />

# MagicTickets Backend

Microservicio backend de MagicTickets, plataforma de venta de tickets para eventos, expuesto vía API REST y respaldado por persistencia real en PostgreSQL. El sistema expone la cartelera de eventos, permite crear eventos nuevos y ejecutar compras validando las mismas reglas de negocio definidas en el Hito 1 (cantidad positiva, máximo 5 tickets por compra, stock suficiente), documentado de forma automática con OpenAPI/Swagger y aislado por perfiles de entorno.

Proyecto correspondiente al **Hito 4** del curso "Java" — Unidad 4: Microservicios con Spring Boot, PostgreSQL y Docker. Evoluciona sobre el dominio entregado en el **Hito 3** (`magictickets`), reconstruido en un repositorio nuevo por introducir un stack tecnológico distinto (Spring Boot, JPA, Docker).

---

## Índice

1. [Pila tecnológica](#pila-tecnológica)
2. [Arquitectura](#arquitectura)
3. [Estructura del repositorio](#estructura-del-repositorio)
4. [Levantar el entorno](#levantar-el-entorno)
5. [Documentación y pruebas de contratos](#documentación-y-pruebas-de-contratos)
6. [API expuesta](#api-expuesta)
7. [Decisiones de diseño](#decisiones-de-diseño)
8. [Suite de tests](#suite-de-tests)
9. [Evidencia de cobertura](#evidencia-de-cobertura)
10. [Continuidad del proyecto](#continuidad-del-proyecto)

---

## Pila tecnológica

- Java 17
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA)
- PostgreSQL 18 (contenedor Docker)
- Hibernate ORM 7
- Springdoc OpenAPI 3.1.0 (Swagger-UI)
- JUnit 5 + Mockito + JaCoCo
- Maven

## Arquitectura

Continúa la Clean Architecture en tres capas del Hito 3, con la capa de infraestructura ahora conectada a un stack productivo real:

- **Domain (`domain/`):** entidad `Event`, Value Object `EventDate`, enums (`ShowStatus`, `ShowCategory`), excepciones de negocio y `PurchaseValidator`. Java puro — sin anotaciones de Spring ni JPA.
- **Application (`application/`):** casos de uso (`EventReadService`, `EventWriteService`, `TicketPurchaseService`) y el puerto `PurchaseNotifier`. Depende únicamente de contratos del dominio.
- **Infrastructure (`infrastructure/`):** controladores REST, manejador global de excepciones, notificador concreto y el adaptador JPA — el anillo que sí conoce Spring, Hibernate y PostgreSQL.

**Divergencia consciente respecto al Hito 3:** `PurchaseValidator` incorpora `@Component` para poder inyectarse vía IoC de Spring, algo que no necesitaba en Hito 3 (instanciado directamente en los tests). Es una tensión real entre "el dominio no conoce el framework" (regla de Hito 3) y "todo se resuelve por inyección de dependencias" (convención de este hito) — se prioriza la segunda por ser el mecanismo estándar de Spring Boot, sin que esto introduzca ninguna dependencia de persistencia o web en la clase.

## Estructura del repositorio

```
magictickets-backend/
├── pom.xml
├── docker-compose.yml
├── README.md
└── src/
    ├── main/java/com/magictickets/backend/
    │   ├── domain/
    │   │   ├── entity/Event.java
    │   │   ├── enums/ShowStatus.java, ShowCategory.java
    │   │   ├── valueobject/EventDate.java
    │   │   ├── exception/ (5 excepciones de negocio)
    │   │   ├── repository/EventRepository.java
    │   │   └── service/PurchaseValidator.java
    │   │
    │   ├── application/
    │   │   ├── EventReadService.java, EventWriteService.java
    │   │   ├── usecase/TicketPurchaseService.java
    │   │   └── port/PurchaseNotifier.java
    │   │
    │   └── infrastructure/
    │       ├── controller/EventController.java, PurchaseController.java
    │       ├── controller/dto/EventRequest.java, PurchaseRequest.java
    │       ├── exception/GlobalExceptionHandler.java
    │       ├── notification/ConsolePurchaseNotifier.java
    │       └── persistence/EventEntity.java, EventJpaRepository.java,
    │           EventRepositoryJpaAdapter.java
    │
    └── test/java/com/magictickets/backend/ (espejo exacto de main)
```

## Levantar el entorno

**1. Base de datos:**

```bash
docker compose up -d
```

Inicia PostgreSQL 18 en un contenedor, expone el puerto `5432` y persiste los datos en un volumen nombrado.

**2. Aplicación:**

```bash
./mvnw spring-boot:run
```

Arranca bajo el perfil `dev` (activo por defecto) en `http://localhost:8080`, conectándose al contenedor levantado en el paso anterior.

## Documentación y pruebas de contratos

- Swagger-UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Ambos quedan deshabilitados bajo el perfil `prod` (`application-prod.yml`), verificado manualmente: acceder a cualquiera de las dos rutas bajo ese perfil retorna `404`.

## API expuesta

| Método | Ruta | Código de éxito | Códigos de error posibles |
|---|---|---|---|
| `GET` | `/api/v1/events` | 200 | — |
| `POST` | `/api/v1/events` | 201 | 400 (fecha inválida) |
| `POST` | `/api/v1/purchases` | 201 | 400, 404, 422 |

Contrato completo, con esquemas de request/response, disponible en Swagger-UI.

## Decisiones de diseño

**Manejo centralizado de errores (`GlobalExceptionHandler`):** cada una de las 5 excepciones de dominio se traduce a un código HTTP semántico:

| Excepción | Código | Motivo |
|---|---|---|
| `EventNotFoundException` | 404 | Recurso solicitado no existe |
| `InvalidQuantityException` | 400 | Dato de entrada inválido |
| `MaxTicketsExceededException` | 400 | Dato de entrada inválido |
| `InvalidEventDateException` | 400 | Dato de entrada inválido |
| `OutOfStockException` | 422 | Regla de negocio violada con sintaxis válida |

**Mapeo JPA de `EventDate`:** el Value Object `EventDate` (record de Hito 3) no puede mapearse como `@Embeddable` porque Hibernate no soporta records ahí (carecen de constructor vacío). Se optó por almacenar un `LocalDate` plano en `EventEntity`, reconstruyendo `EventDate` manualmente en `toDomain()`. Queda fuera de alcance (YAGNI) validar datos insertados directamente en la base de datos sin pasar por la aplicación.

**Reemplazo de `InMemoryEventRepository`:** la implementación en memoria de Hito 3 generaba un conflicto de Bean ambiguo con `EventRepositoryJpaAdapter` (dos `@Repository` implementando `EventRepository`). Se eliminó del repositorio del backend; permanece intacta en `magictickets` (Hito 3), sin que su ausencia aquí represente una regresión.

**Alcance del flujo de compra:** `TicketPurchaseService` migra completo desde Hito 3 (incluyendo `PurchaseNotifier`, aunque su propósito original fue justificar el uso de Mockito en Hito 1) para poder demostrar en ejecución real las cuatro validaciones de negocio y sus códigos HTTP correspondientes — no solo como arquitectura sin probar.

## Suite de tests

Framework: **JUnit 5** + **Mockito**. Patrón **AAA** en los 40 tests. Suite generada con asistencia de Claude Code, revisada y corregida manualmente antes de su integración.

| Clase de test | Tests | Qué cubre |
|---|---|---|
| `PurchaseValidatorTest` | 8 | Las tres reglas de validación, sin mocks |
| `EventTest` | 6 | Creación, categoría nula, estado inicial, reducción de stock |
| `EventDateTest` | 4 | Auto-validación del Value Object |
| `TicketPurchaseServiceTest` | 5 | Orquestación completa, con `EventRepository`/`PurchaseNotifier` mockeados |
| `EventReadServiceTest` | 2 | Delegación al repositorio |
| `EventWriteServiceTest` | 2 | Creación y persistencia de eventos |
| `EventControllerTest` | 2 | `@WebMvcTest`: códigos HTTP y JSON de `/api/v1/events` |
| `PurchaseControllerTest` | 2 | `@WebMvcTest`: creación exitosa y propagación de 404 |
| `GlobalExceptionHandlerTest` | 5 | Código HTTP correcto por cada excepción de dominio |
| `EventRepositoryJpaAdapterTest` | 3 | `@DataJpaTest` con H2: mapeo `Event ↔ EventEntity` |
| `MagicticketsBackendApplicationTests` | 1 | Arranque del contexto completo (requiere PostgreSQL activo) |

**Nota sobre `EventRepositoryJpaAdapterTest`:** usa H2 en memoria (perfil `test`, `application-test.yml`) exclusivamente para poder ejecutar la capa de persistencia sin depender de que Docker esté corriendo. El resto de la suite corre bajo el perfil `dev`; `MagicticketsBackendApplicationTests` sí requiere el contenedor de PostgreSQL activo, al conectarse contra la configuración real.

## Evidencia de cobertura

Reporte HTML en `target/site/jacoco/index.html`.

- **Instructions:** 97% (12 de 534 sin cubrir)
- **Branches:** 100% (0 de 16)

Cobertura por paquete: 100% en `domain` (todas sus subcarpetas), `application`, `infrastructure.controller`, `infrastructure.exception` e `infrastructure.persistence`. Dos paquetes por debajo del 100%:

- `infrastructure.notification` (50%): `ConsolePurchaseNotifier` no tiene un test dedicado que verifique la línea de log — no se pidió explícitamente en el alcance de la suite.
- `com.magictickets.backend` (37%, paquete raíz): la clase de arranque `MagicticketsBackendApplication`, cuyo único método (`main`) no se ejecuta línea a línea en un test de contexto (`contextLoads`).

Ninguno de los dos vacíos corresponde a lógica de negocio ni a código de la rúbrica evaluable (Pilares 1, 2 y 3 del Hito 4); se documentan aquí por transparencia, no se completan artificialmente.

---

## Continuidad del proyecto

Repositorio nuevo respecto a `magictickets` (Hitos 1 y 3): Spring Boot, Spring Data JPA, PostgreSQL y Docker constituyen un stack distinto, aun cuando el lenguaje siga siendo Java. El dominio de Hito 3 se copió manualmente con corrección de packages; ambos repositorios permanecen independientes y sin tags cruzados.


## JaCoCo

<img width="1531" height="510" alt="imagen" src="https://github.com/user-attachments/assets/3a0c9265-3333-4dcc-a854-38cb1dbe9df1" />

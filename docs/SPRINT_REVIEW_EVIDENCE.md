# Development Evidence for Sprint Review

Durante este Sprint se implementó la extracción del contexto Account como un microservicio independiente y su integración mediante un API Gateway. El backend incorpora registro transaccional de identidad y perfil, autenticación BCrypt/JWT, autorización por propietario y rol, migraciones Flyway y validación de sesiones desde el monolito. El frontend se adaptó al contrato Account mediante el almacenamiento de sesión, protección de rutas, consulta y actualización de perfiles y manejo de cambios de contraseña. Los contextos Incidents, Emergencies y Communities mantienen su implementación existente; en el backend permanecen en el monolito y se integran con la identidad administrada por Account.

Los repositorios se organizan mediante ramas main, develop y feature por contexto. Los commits siguientes son la importación inicial del trabajo local al control de versiones y registran su estado actual. No reconstruyen fechas o autoría de desarrollos anteriores ni afirman que los otros tres contextos ya sean microservicios independientes. La publicación en main es una versión del código; no constituye un despliegue en producción.

| Repository | Branch | Commit Id | Commit Message | Commit Message Body | Committed on (Date, America/Lima) |
|---|---|---|---|---|---|
| [DiazDeveloper/instalert-backend-microservices](https://github.com/DiazDeveloper/instalert-backend-microservices) | feature/account | [d7fb751](https://github.com/DiazDeveloper/instalert-backend-microservices/commit/d7fb7519bdf3b4b5c6a09b374a03229fc32d8ff4) | feat(account): extract Account service and integrate API Gateway | Import Account identities, profiles, BCrypt/JWT security, Flyway migrations, Gateway routing and monolith security transition. Legacy IAM/Profile sources are retained but excluded from active monolith scanning. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-backend-microservices](https://github.com/DiazDeveloper/instalert-backend-microservices) | feature/incidents | [329c421](https://github.com/DiazDeveloper/instalert-backend-microservices/commit/329c421b17ae2c917c14a0bf9c3ed7377ebfb206) | feat(incidents): import monolith context with Account authorization | Import the existing incidents bounded context from the local project. This context remains in the monolith; it is not an extracted microservice in this Sprint. Ownership uses the authenticated Account reference. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-backend-microservices](https://github.com/DiazDeveloper/instalert-backend-microservices) | feature/emergencies | [f1cb677](https://github.com/DiazDeveloper/instalert-backend-microservices/commit/f1cb677852af4890adf028ba1bf6698aea6f1be0) | feat(emergencies): import monolith context with Account authorization | Import the existing emergencies bounded context from the local project. This context remains in the monolith; it is not an extracted microservice in this Sprint. Ownership uses the authenticated Account reference. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-backend-microservices](https://github.com/DiazDeveloper/instalert-backend-microservices) | feature/communities | [1334a74](https://github.com/DiazDeveloper/instalert-backend-microservices/commit/1334a74a8d3ac51f960b890e332b08431dfcbbe5) | feat(communities): import monolith context with Account authorization | Import the existing communities bounded context from the local project. This context remains in the monolith; it is not an extracted microservice in this Sprint. Ownership uses the authenticated Account reference. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-frontend-microservices](https://github.com/DiazDeveloper/instalert-frontend-microservices) | feature/account | [02687c3](https://github.com/DiazDeveloper/instalert-frontend-microservices/commit/02687c33e081c52e277d76480b43d4ae1e93a7de) | feat(account): integrate Account authentication and protected profile flows | Import Account UI, user store, guards, scoped JWT interceptor and administrative views. Authentication uses the Gateway and Account session; no authentication bypass is introduced. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-frontend-microservices](https://github.com/DiazDeveloper/instalert-frontend-microservices) | feature/incidents | [e964ac3](https://github.com/DiazDeveloper/instalert-frontend-microservices/commit/e964ac38c6770806a9840746b17b96d37284574e) | feat(incidents): import UI context with Account integration | Import the existing incidents bounded context from the local project. The screens continue using the corresponding backend context through the Gateway. This is an import of existing functionality, not a newly completed microservice. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-frontend-microservices](https://github.com/DiazDeveloper/instalert-frontend-microservices) | feature/emergencies | [8d4ece6](https://github.com/DiazDeveloper/instalert-frontend-microservices/commit/8d4ece638c86150d73c94f882d05b9ae9ff65761) | feat(emergencies): import UI context with Account integration | Import the existing emergencies bounded context from the local project. The screens continue using the corresponding backend context through the Gateway. This is an import of existing functionality, not a newly completed microservice. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-frontend-microservices](https://github.com/DiazDeveloper/instalert-frontend-microservices) | feature/communities | [0f595b8](https://github.com/DiazDeveloper/instalert-frontend-microservices/commit/0f595b8b53aecfea6f912afbdb41cebacb733cf4) | feat(communities): import UI context with Account integration | Import the existing communities bounded context from the local project. The screens continue using the corresponding backend context through the Gateway. This is an import of existing functionality, not a newly completed microservice. | 09/10/2026 21:58 |

# Testing Suite Evidence for Sprint Review

Se versionaron pruebas BDD con Cucumber y archivos .feature escritos en Gherkin para verificar el registro, el rechazo de credenciales incorrectas y la autorización de perfiles. Los escenarios ejecutan solicitudes HTTP con Spring MockMvc, el contexto real de Account y persistencia H2 preparada con Flyway. Se complementan con pruebas de integración de Account, compatibilidad de esquema, enrutamiento del Gateway, transición de seguridad del monolito y regresiones del frontend.

| Test diseñado | User Story | Verificación |
|---|---|---|
| Registration creates an identity with an associated profile | HU1: registro de cuenta | Devuelve 201, asocia el perfil al UUID y asigna ROLE_USER. |
| Invalid credentials cannot open a session | HU2: autenticación | Una contraseña incorrecta recibe HTTP 401. |
| A citizen cannot edit another account | HU3: perfil y autorización | La modificación de otro perfil recibe HTTP 403 y conserva su correo original. |

## Archivo Gherkin versionado

Repositorio de testing de Web Services: https://github.com/DiazDeveloper/instalert-backend-microservices

Archivo: [account.feature](https://github.com/DiazDeveloper/instalert-backend-microservices/blob/main/services/ms-account/src/test/resources/features/account.feature)

Step definitions: [AccountSteps.java](https://github.com/DiazDeveloper/instalert-backend-microservices/blob/main/services/ms-account/src/test/java/com/instalert/account/bdd/AccountSteps.java)

Runner: [AccountBehaviorTest.java](https://github.com/DiazDeveloper/instalert-backend-microservices/blob/main/services/ms-account/src/test/java/com/instalert/account/bdd/AccountBehaviorTest.java)

```gherkin
Feature: Account registration, authentication and profile authorization
  Scenario: Registration creates an identity with an associated profile
    When a citizen registers a complete account
    Then registration succeeds with an associated profile and citizen authority

  Scenario: Invalid credentials cannot open a session
    Given a registered citizen
    When the citizen signs in with an incorrect password
    Then the request is rejected with status 401

  Scenario: A citizen cannot edit another account
    Given two registered citizens
    When the first citizen edits the second citizen profile
    Then the request is rejected with status 403
    And the second citizen email remains unchanged
```

Los tests están incluidos en los mismos repositorios de implementación, no en un tercer repositorio separado. La suite frontend es Vitest/Angular; no se presenta como Gherkin ni como testing de Web Services.

## Commits relacionados con testing

| Repository | Branch | Commit Id | Commit Message | Commit Message Body | Committed on (Date, America/Lima) |
|---|---|---|---|---|---|
| [DiazDeveloper/instalert-backend-microservices](https://github.com/DiazDeveloper/instalert-backend-microservices) | feature/account-testing | [30b6dc9](https://github.com/DiazDeveloper/instalert-backend-microservices/commit/30b6dc91c16d0e9ffb2c7af8fb34076f294c1a0a) | test(account): import BDD and integration verification suite | Version Cucumber Gherkin features and step definitions, Account integration and migration tests, Gateway/monolith tests, and offline migration verification. HU1 registration, HU2 authentication, HU3 profile authorization. Test data uses H2 and synthetic accounts. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-frontend-microservices](https://github.com/DiazDeveloper/instalert-frontend-microservices) | feature/account-testing | [db9ed07](https://github.com/DiazDeveloper/instalert-frontend-microservices/commit/db9ed07e559e28e8dd397a92aaafc550146e18ed) | test(account): import session interceptor and navigation regression tests | Version the Account-focused eight-test suite and original component specs. The verified command is npm run test:account; the inherited full test suite is not claimed as passing. | 09/10/2026 21:58 |

## Resultado de ejecución local previo a la publicación

| Suite | Resultado |
|---|---|
| AccountIntegrationTest | 9 pruebas aprobadas |
| AccountBehaviorTest (Cucumber) | 3 escenarios aprobados |
| OriginalSchemaMigrationTest | 1 prueba aprobada |
| GatewayIntegrationTest | 3 pruebas aprobadas |
| Contexto del monolito y transición Account | 4 pruebas aprobadas |
| Preparación offline de migración (Python) | 3 pruebas aprobadas |
| Account frontend (Vitest/Angular) | 8 pruebas aprobadas |
| Total | 31 verificaciones aprobadas |

Los resultados anteriores corresponden a suites concretas; no se afirma que toda la suite heredada del frontend pase. Las pruebas Java usan H2; la compatibilidad con MySQL local y la interacción Postman/Swagger se documentan como evidencias separadas. No se verificó un despliegue cloud ni una ejecución Docker de MySQL 8.4.

Comandos desde la raíz del backend, con JDK 21:

```powershell
./mvnw.cmd -f services/ms-account/pom.xml verify
./mvnw.cmd -f services/api-gateway/pom.xml verify
./mvnw.cmd verify
python -m unittest discover -s scripts -p 'test_*.py'
```

Comando desde la raíz del frontend:

```powershell
npm run test:account
```

El wrapper puede requerir acceso a Maven Central en una instalación nueva. En esta sesión las suites Java se ejecutaron con el Maven 3.9.16 de la instalación local y JDK 21. Los reportes generados están en target/surefire-reports y services/*/target/surefire-reports; no se publican archivos temporales o logs con datos de sesiones.

# Commits de documentación para Microservices Documentation Evidence

| Repository | Branch | Commit Id | Commit Message | Commit Message Body | Committed on (Date, America/Lima) |
|---|---|---|---|---|---|
| [DiazDeveloper/instalert-backend-microservices](https://github.com/DiazDeveloper/instalert-backend-microservices) | feature/account-documentation | [aabba06](https://github.com/DiazDeveloper/instalert-backend-microservices/commit/aabba06d79c285603672b2e131218d98d4e4b412) | docs(account): document OpenAPI contract Swagger and migration | Version the local Sprint documentation, implemented scope, validation instructions and known limitations. Documentation describes Account extraction and retains the other three contexts as existing functionality. | 09/10/2026 21:58 |
| [DiazDeveloper/instalert-frontend-microservices](https://github.com/DiazDeveloper/instalert-frontend-microservices) | feature/account-documentation | [2fc07de](https://github.com/DiazDeveloper/instalert-frontend-microservices/commit/2fc07de67bea9962306f53efcab1a2e62fe12d5e) | docs(account): document Account migration and frontend validation | Version the local Sprint documentation, implemented scope, validation instructions and known limitations. Documentation describes Account extraction and retains the other three contexts as existing functionality. | 09/10/2026 21:58 |

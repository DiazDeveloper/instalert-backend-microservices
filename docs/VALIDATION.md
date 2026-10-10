# Evidencia de verificación local — 2026-10-09

La tabla siguiente registra la primera validación. La preparación posterior con MySQL 9.7 real está documentada en [WebStorm local](WEBSTORM_LOCAL.md): cuatro procesos arrancados, frontend HTTP 200 y ocho comprobaciones de API aprobadas. Se conservaron tablas y restricciones existentes, se añadieron campos con Flyway V2 y se crearon usuarios separados. La cuenta temporal de verificación fue retirada. Docker permanece pendiente.

Resultados obtenidos sobre el código adaptado, sin datos reales ni conexiones al MySQL existente.

| Verificación | Resultado |
| --- | --- |
| Account: integración de registro, asociación independiente de IDs, seguridad, contraseña y perfiles | 9 pruebas aprobadas |
| Account: Cucumber, archivo feature, steps y runner | 3 escenarios aprobados |
| Monolito: arranque de contexto | 1 prueba aprobada |
| Monolito: exclusión IAM/Profiles, sesión HTTP y protección de referencias | 3 pruebas aprobadas |
| Gateway: rutas y preservación de errores con/sin cuerpo | 3 pruebas aprobadas |
| Angular: registro/sesión e interceptor | 7 pruebas aprobadas |
| Preparación offline de migración | 3 pruebas aprobadas |
| Total automatizado | 29 pruebas aprobadas, cero fallos en estas suites |
| Integración HTTP con 3 procesos independientes | 15 comprobaciones aprobadas |
| Maven verify de Account, gateway y monolito | Aprobado con JDK 21 |
| Angular build de producción | Aprobado |

La integración usó H2 en modo MySQL y datos sintéticos en memoria. Se comprobó registro de dos cuentas, consulta propia, edición de terceros rechazada sin cambio de datos, login inválido, listado administrativo restringido, creación de comunidades/reportes/alertas, rechazo de cancelación por otro usuario y revocación de acceso tanto en Account como en el monolito tras cambiar contraseña.

Se corrigieron con evidencia HTTP dos problemas de integración: el redespacho de errores de Spring podía reemplazar un 403 por 401, y el cliente HTTP inicial del gateway convertía errores sin cuerpo en 502. Hay comprobaciones que cubren el comportamiento final.

La suite histórica completa del frontend NO está aprobada: al ejecutar pruebas se encontraron imports de componentes inexistentes en los tests previos. `npm run test:account` usa una configuración específica con las pruebas nuevas. El build conserva advertencias previas sobre Leaflet/CommonJS y 190 bytes sobre el presupuesto CSS de reportes.

En la primera ejecución no se verificaron contenedores ni MySQL 8.4 porque Docker no está disponible. MySQL 9.7 local fue verificado posteriormente con autorización, como se describe arriba. No se ejecutó migración real, despliegue Azure, pruebas de carga o reversión productiva. La documentación de Account explica cómo hacer esa validación sobre una copia autorizada.

Runtime de verificación: Temurin JDK 21 portátil en `.tools`, sin cambio de Java global. Distribución obtenida mediante la [API oficial de Adoptium](https://adoptium.net/installation/ci-scripts/).

Evidencia regenerable: reportes Surefire en `target/surefire-reports` de cada aplicación, reporte Cucumber en `services/ms-account/target/cucumber/account.json`, scripts y suites versionables en las fuentes.

Última integración: 2026-10-09T21:27:22.7787728Z UTC; resultado `passed`.

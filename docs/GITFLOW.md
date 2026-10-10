# Gitflow — Sprint Account

- main: versión integrada inicial del Sprint; no implica despliegue.
- develop: integración y punto de partida para el siguiente Sprint.
- feature/account: importación de la implementación Account.
- feature/incidents: importación del contexto existente.
- feature/emergencies: importación del contexto existente.
- feature/communities: importación del contexto existente.
- feature/account-testing: suites verificadas del Sprint.
- feature/account-documentation: contrato/guías correspondientes.
- feature/sprint-review-evidence: tablas con commits reales y trazabilidad de pruebas.

Ramas feature parten de develop y se integran con merge --no-ff. Cuando haya una entrega posterior se puede crear release/<version> desde develop y, para correcciones urgentes de una versión entregada, hotfix/<descripcion> desde main. No se crean ramas release/hotfix sin trabajo correspondiente.

Commits: feat(context): funcionalidad/importación; test(account): pruebas; docs(account): documentación; chore: configuración. Gitflow define las ramas; Conventional Commits define estos mensajes. Las fechas son reales de la importación, no una reconstrucción de desarrollo pasado. No se publican .env.local, contraseñas de BD, JWT reales, tokens GitHub, node_modules, target, dist o logs.

Solo Account fue extraído como microservicio. Los otros tres contextos continúan en el monolito. Las ramas representan organización y trazabilidad, no una afirmación de extracción de todos los servicios.

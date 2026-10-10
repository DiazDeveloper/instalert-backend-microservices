# Swagger local de Account

Documentación: http://localhost:8090/swagger-ui/index.html

Contrato OpenAPI servido: http://localhost:8090/openapi.json

Contrato fuente: `services/ms-account/openapi.json` (12 operaciones).

El servidor de documentación escucha solo en loopback. Sirve Swagger UI y reenvía únicamente las operaciones Account declaradas en OpenAPI al Gateway fijo `http://127.0.0.1:8080`. Esto permite usar Try it out sin modificar CORS o la seguridad de los servicios. El puerto 8090 es documentación; 8080 sigue siendo el Gateway. No sirve el repositorio completo ni los archivos de entorno. Los ejemplos del contrato son ficticios; el JWT de ejemplo no funciona como credencial.

## Arranque

Mantener Account y Gateway ejecutándose con `scripts/Start-Local.ps1`. Mantener también el monolito para las otras funciones de la aplicación.

Descargar los assets oficiales una vez desde la raíz del backend:

```powershell
New-Item -ItemType Directory -Force target/swagger-assets | Out-Null
Invoke-WebRequest 'https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css' -OutFile target/swagger-assets/swagger-ui.css
Invoke-WebRequest 'https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js' -OutFile target/swagger-assets/swagger-ui-bundle.js
```

Ejecutar en una terminal adicional:

```powershell
& 'C:/Users/Usuario/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' scripts/serve_account_docs.py
```

También puede usarse un Python 3 instalado: `python scripts/serve_account_docs.py`.

Ctrl+C detiene solo el servidor de documentación. Los assets descargados quedan en target, que está ignorado por Git. Fuente de instalación de Swagger UI: https://swagger.io/docs/open-source-tools/swagger-ui/usage/installation/

## Interacción para evidencia

1. Abrir Swagger y fotografiar el catálogo de Account (Autenticación, Account, Perfiles y Administración).
2. Expandir POST `/authentication/sign-in`.
3. Pulsar Try it out; introducir las credenciales de una cuenta local de muestra ya existente.
4. Pulsar Execute. Fotografiar el request body y el Server response real 200. Ocultar JWT completo antes de compartir la imagen.
5. Copiar el token real recibido. En Authorize pegar solo el token, sin la palabra Bearer, pulsar Authorize y Close.
6. Expandir GET `/account/me`, Try it out y Execute. Fotografiar Server response 200 y los datos de muestra.
7. Expandir PUT `/users/{accountId}`. Fotografiar la definición del parámetro UUID, el body y las respuestas documentadas. Para probarlo, introducir el UUID de la propia cuenta y su correo correcto; los UUID y correos del ejemplo son ficticios.
8. Expandir GET `/users`, Try it out y Execute con un token ciudadano válido. Fotografiar el Server response 403 y explicar la restricción administrativa.

Las respuestas de Example Value son ejemplos del contrato; no son evidencia de una ejecución. Para evidencia de interacción capturar también Server response después de Execute.

# Microservices Documentation Evidence for Sprint Review

Durante este Sprint se elaboró la documentación del microservicio Account mediante un contrato OpenAPI 3.0.3. Se documentaron doce operaciones de registro, autenticación, consulta de cuentas y perfiles, actualización de datos, cambio de contraseña y administración de roles y estado de las cuentas. Cada operación especifica el método HTTP, la ruta, los parámetros o cuerpo de entrada, los requisitos de autorización y las respuestas correspondientes. La documentación se presenta mediante Swagger UI en un entorno local y permite ejecutar solicitudes de muestra a través del API Gateway.

El alcance de esta sección corresponde a Account. Los contextos de incidentes, emergencias y comunidades permanecen en el monolito y no se presentan como nuevos microservicios de este Sprint.

- Swagger local: http://localhost:8090/swagger-ui/index.html
- Contrato OpenAPI local: http://localhost:8090/openapi.json
- Base de llamadas al Gateway: http://localhost:8080/api/v1
- Archivo versionable: services/ms-account/openapi.json

## Relación de operaciones documentadas

Las rutas de la tabla se añaden a la base `http://localhost:8080/api/v1`. `accountId` es un UUID, no el identificador numérico del perfil. No hay parámetros de consulta en estas doce operaciones. Las operaciones protegidas requieren el encabezado `Authorization: Bearer <JWT válido>`.

| Acción implementada | Verbo y sintaxis | Parámetros / cuerpo / permiso | Ejemplo y explicación de respuesta | Documentación |
|---|---|---|---|---|
| Registrar cuenta | POST `/authentication/sign-up` | JSON: name, email y password obligatorios; phone, birthDate y gender opcionales. Pública. | 201: objeto con accountId, email, token y profile. Crea identidad, perfil y asociación. 400 datos inválidos; 409 correo duplicado/conflicto. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Autenticaci%C3%B3n/post_authentication_sign_up) |
| Iniciar sesión | POST `/authentication/sign-in` | JSON: email y password obligatorios. Pública. | 200: sesión con JWT y perfil. 400 formato inválido; 401 credenciales incorrectas o cuenta desactivada. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Autenticaci%C3%B3n/post_authentication_sign_in) |
| Consultar mi perfil | GET `/account/me` | JWT válido. Sin body ni parámetros. | 200: profile con id, accountId, name, email, role, roles, currentLocation, avatar, phone, birthDate y gender. 401 sin sesión válida. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Account/get_account_me) |
| Consultar sesión | GET `/account/session` | JWT válido. Sin body ni parámetros. | 200: `{"accountId":"UUID","referenceId":1,"roles":["ROLE_USER"]}`. Resuelve identidad y referencia para integración. 401 sin sesión válida. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Account/get_account_session) |
| Consultar directorio | GET `/account/directory` | JWT válido. Sin body ni parámetros. | 200: `[{"id":1,"accountId":"UUID","name":"Usuario Sprint","avatar":""}]`. Directorio básico de cuentas activas; omite datos privados. 401 sin sesión válida. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Account/get_account_directory) |
| Contar cuentas activas | GET `/account/count` | JWT válido. Sin body ni parámetros. | 200: `{"count":3}`. Total de cuentas activas; puede variar. 401 sin sesión válida. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Account/get_account_count) |
| Consultar perfil por UUID | GET `/users/{accountId}` | Path accountId obligatorio. Propietario o administrador. | 200: profile de la cuenta objetivo. 401 sin sesión válida; 403 sin acceso; 404 cuenta activa inexistente. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Perfiles/get_users_accountId) |
| Actualizar perfil | PUT `/users/{accountId}` | Path accountId. JSON: email obligatorio; name, phone, birthDate y gender opcionales. Propietario o administrador. | 200: profile actualizado. 400 datos inválidos; 401 sin sesión; 403 sin acceso; 404 cuenta inexistente; 409 correo duplicado/conflicto. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Perfiles/put_users_accountId) |
| Listar perfiles completos | GET `/users` | JWT con ROLE_ADMIN. Sin body ni parámetros. | 200: lista de objetos profile de cuentas activas. 401 sin sesión válida; 403 si quien llama es ciudadano. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Administraci%C3%B3n/get_users) |
| Cambiar rol | PUT `/users/{accountId}/role` | Path accountId. JSON: role obligatorio, ROLE_USER o ROLE_ADMIN. Requiere administrador. | 200: profile con nuevo rol; revoca sesiones de la cuenta modificada. 400 rol inválido; 401 sin sesión; 403 sin permisos; 404 cuenta inexistente; 409 conflicto. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Administraci%C3%B3n/put_users_accountId_role) |
| Desactivar cuenta | DELETE `/users/{accountId}` | Path accountId. Sin body. Requiere administrador. | 204 sin cuerpo: cuenta desactivada y sesiones revocadas. Conserva referencias. 401 sin sesión; 403 sin permisos; 404 cuenta inexistente; 409 conflicto. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Administraci%C3%B3n/delete_users_accountId) |
| Cambiar contraseña | POST `/authentication/{accountId}/change-password` | Path accountId. JSON: currentPassword y newPassword obligatorios. Solo propietario. | 204 sin cuerpo: contraseña modificada y sesiones anteriores revocadas. 400 contraseña actual/datos inválidos; 401 sin sesión; 403 cuenta ajena; 404 inexistente; 409 conflicto. | [Swagger](http://localhost:8090/swagger-ui/index.html#/Autenticaci%C3%B3n/post_authentication_accountId_change_password) |

Las operaciones protegidas pueden responder 503 si la persistencia requerida para validar la sesión no está disponible. Las contraseñas nuevas admiten entre 8 y 72 caracteres y no más de 72 bytes UTF-8. birthDate utiliza formato YYYY-MM-DD y no admite fechas futuras. Los cuerpos no admiten propiedades desconocidas. RoleChange también admite los alias Admin y Ciudadano verificado por compatibilidad.

## Ejemplos de cuerpos de entrada

Datos ficticios. Para ejecutar las pruebas se debe crear primero la cuenta de muestra o utilizar una ya existente; el UUID y JWT de Example Value no son credenciales reales.

Registro:
```json
{"name":"Usuario Sprint","email":"swagger.sprint@example.com","password":"SwaggerSprint2026!","phone":"999888777","birthDate":"2000-05-15","gender":"Masculino"}
```

Login:
```json
{"email":"swagger.sprint@example.com","password":"SwaggerSprint2026!"}
```

Actualización:
```json
{"name":"Usuario Sprint Actualizado","email":"swagger.sprint@example.com","phone":"988777666","birthDate":"2000-05-15","gender":"Masculino"}
```

Cambio de rol:
```json
{"role":"ROLE_USER"}
```

Cambio de contraseña:
```json
{"currentPassword":"SwaggerSprint2026!","newPassword":"SwaggerSprint2027!"}
```

## Ejemplos de respuestas

Sesión de registro (201) o login (200):
```json
{
  "accountId":"11111111-1111-4111-8111-111111111111",
  "email":"swagger.sprint@example.com",
  "token":"JWT_EMITIDO_POR_EL_SERVICIO",
  "profile":{
    "id":1,"accountId":"11111111-1111-4111-8111-111111111111",
    "name":"Usuario Sprint","email":"swagger.sprint@example.com",
    "role":"Ciudadano verificado","roles":["ROLE_USER"],
    "currentLocation":"","avatar":"","phone":"999888777",
    "birthDate":"2000-05-15","gender":"Masculino"
  }
}
```

Las consultas y modificaciones de perfil devuelven directamente el objeto profile mostrado arriba, con los valores reales o modificados según la operación. El listado administrativo devuelve una lista de esos objetos. El directorio, la sesión y el contador devuelven las estructuras reducidas de la tabla. Las operaciones 204 no devuelven cuerpo.

## Capturas de interacción con Swagger

### Imagen 1: Catálogo de operaciones

La siguiente captura presenta la documentación OpenAPI del microservicio Account organizada en Autenticación, Account, Perfiles y Administración. Se muestran las doce operaciones del Sprint con sus métodos HTTP y rutas.

[Insertar captura del catálogo actual de Swagger Account]

El catálogo permite identificar el alcance documentado y distinguir las operaciones públicas de aquellas que requieren autenticación.

### Imagen 2: Parámetros y esquema de actualización

Se despliega la operación PUT `/users/{accountId}` para visualizar el parámetro UUID obligatorio, el cuerpo JSON, las restricciones de acceso y las respuestas documentadas.

[Insertar captura de parámetros, body y respuestas de PUT users]

La documentación indica que el identificador corresponde al accountId y que la modificación solo está permitida al propietario o a un administrador. El ejemplo del cuerpo facilita construir una solicitud válida.

### Imagen 3: Inicio de sesión desde Swagger

Se utiliza Try it out en POST `/authentication/sign-in` con una cuenta local de muestra. La solicitud se ejecuta desde Swagger UI y se envía al servicio a través del API Gateway.

[Insertar captura del request body y Server response real 200; ocultar JWT completo]

La respuesta 200 confirma la autenticación y devuelve el perfil y un token JWT. Esta evidencia corresponde a una ejecución real; se distingue del apartado Example Value, que contiene ejemplos estáticos del contrato.

### Imagen 4: Consulta autenticada del perfil

Después de configurar el JWT mediante Authorize, se ejecuta GET `/account/me` desde la documentación interactiva.

[Insertar captura de Server response real 200 con datos de muestra]

La respuesta muestra el perfil de la cuenta autenticada y evidencia que la autorización Bearer documentada permite probar las operaciones protegidas.

### Imagen 5: Restricción administrativa

Se ejecuta GET `/users` con el token válido de una cuenta ciudadana para comprobar el requisito de autorización administrativa descrito en OpenAPI.

[Insertar captura de Server response real 403]

La respuesta 403 confirma que el servicio reconoce la sesión, pero rechaza la operación por falta de ROLE_ADMIN. Esto evidencia el comportamiento de seguridad documentado.

## Repositorio y commits de documentación

- URL del repositorio de Web Services: [PENDIENTE: URL real del repositorio del equipo].
- Commit del contrato OpenAPI: [PENDIENTE: SHA real después de subir services/ms-account/openapi.json].
- Commit de Swagger y guía local: [PENDIENTE: SHA real después de subir scripts/serve_account_docs.py y docs/SWAGGER_LOCAL.md].

Si un único commit incluye ambos cambios, indicar el mismo SHA y describir su alcance. La copia local revisada no contiene .git; no se han creado commits ni se pueden verificar hashes desde esta carpeta. No usar hashes inventados ni un repositorio antiguo como evidencia de cambios que aún no se han publicado.

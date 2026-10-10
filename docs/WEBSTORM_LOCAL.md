# Arranque local preparado para WebStorm

Se verificó la conexión local MySQL 9.7 en localhost:3306 y las bases existentes `instalert_account` y `instalert_db`. Las tablas y filas anteriores se conservaron. El SQL original de Account quedó registrado mediante baseline V1 y la migración V2 añadió los campos necesarios, respetando `account_status`, su restricción, el UUID CHAR(36) y los roles SMALLINT. No se activaron los otros microservicios: sus tablas propuestas permanecen sin uso en este sprint.

Se crearon usuarios de aplicación separados. El monolito solo tiene permisos DML sobre sus tres tablas; Account tiene permisos sobre su propia base para ejecutar Flyway. La contraseña administrativa no se guardó en las fuentes. Las credenciales de aplicación y la clave JWT están en `.env.local`, ignorado por Git. No mostrar ni compartir ese archivo.

La primera preparación ya dejó las cuatro aplicaciones ejecutándose. Abrir **http://localhost:4200**, usando `localhost` como origen autorizado. Registrarse con una cuenta propia; no hay usuarios ni contraseñas de demostración predefinidos. La cuenta temporal de verificación se retiró.

Para detener únicamente los procesos registrados durante esta preparación, ejecutar en la terminal del backend:

```powershell
pwsh -NoProfile -File .\scripts\Stop-PreparedLocal.ps1
```

Para volver a arrancar, abrir tres pestañas de terminal en la raíz del backend y ejecutar un comando en cada una. Los scripts cargan la configuración privada y seleccionan el JDK 21 portátil; no es necesario copiar contraseñas a la terminal:

```powershell
pwsh -NoProfile -File .\scripts\Start-Local.ps1 -Service Account
```

```powershell
pwsh -NoProfile -File .\scripts\Start-Local.ps1 -Service Monolith
```

```powershell
pwsh -NoProfile -File .\scripts\Start-Local.ps1 -Service Gateway
```

En la terminal de la raíz del frontend:

```powershell
npm start
```

Esperar a los mensajes `Started ...Application` del backend y la dirección de Angular. Los procesos ocupan Account 8081, monolito 8082, gateway 8080 y Angular 4200. No ejecutar una segunda copia si ya están activos. Al iniciarlos tú en terminales, detener cada uno con Ctrl+C; el script de detención anterior solo reconoce los procesos de la preparación original y evita matar PIDs reutilizados.

Configuración local verificada el 2026-10-09: frontend HTTP 200, ocho comprobaciones de API por gateway (registro real en MySQL, perfil propio, listado protegido y consultas de los tres contextos). No se importaron datos antiguos ni se borraron bases. También pasaron trece pruebas de Account, BDD y conservación del esquema original, además de las tres pruebas de preparación de migración; resultados en los reportes Surefire.

## Acceso local de demostración y corrección del login

El 2026-10-09 se reprodujo el login completo en el navegador. Un efecto de LoginView/RegisterView volvía a navegar cada vez que los guards refrescaban el perfil, interrumpiendo la navegación. Ahora la navegación ocurre una sola vez al aceptar la sesión, y un test con RouterTestingHarness cubre ambos guards. Las ocho pruebas de frontend Account pasan; el navegador confirmó acceso al dashboard con la cuenta del usuario y con la cuenta de demostración.

Acceso de demostración ciudadano: `demo@instalert.local`, contraseña `DemoInstalert2026!`. Se creó mediante la API normal, con BCrypt/JWT y sin autoridades administrativas. No hay bypass, token fijo ni fixture activado en producción. Para prepararlo en otra base local, ejecutar manualmente `pwsh -NoProfile -File .\scripts\Create-LocalDemo.ps1`; solo apunta a localhost y no reemplaza contraseñas de cuentas existentes.

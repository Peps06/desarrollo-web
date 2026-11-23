# Bitácora: Clonación y ejecución en otro entorno WSL

- **Fecha de la prueba:** 22-11-2026
- **Distribución limpia:** `Ubuntu-22.04` (Ubuntu 22.04.5 LTS, WSL 2, kernel 6.18.33.2-microsoft-standard-wsl2), usuario `prueba`
- **Distribución de desarrollo:** `Ubuntu` (predeterminada). No se usó. Se detuvo una vez con `wsl --terminate Ubuntu` (ver Falla 1).
- **Versión probada:** `main` en `b165038` ("Pruebas listas"), igual en local y en GitHub al momento de clonar.
- **Regla para ejecutar:** se siguió únicamente el `README.md` de la raíz y en el orden en que estaba escrito. Todo lo que hubo que hacer fuera del README se registra en este archivo.

## Resumen

El proyecto se pudo correr en una distribución limpia siguiendo el README, pero el README tenía tres problemas que ya se corrigieron:

1. La instalación de MySQL falla si otra distribución de WSL tiene MySQL encendido porque comparten el mismo puerto que es el 3306. Se agregó el caso a "Problemas comunes".
2. "Configuración local" pedía entrar a la carpeta del repositorio antes de que se explicara cómo clonarlo. La clonación se pasó a una sección propia, antes de la configuración.
3. La aplicación no puede crear las tablas `actividad` y `gasto` en MySQL 8. Ahora el README indica cargar `database/esquema.sql` antes del primer arranque.

## Pasos del README en orden

| # | Paso | Comando del README | Resultado |
| - | ---- | ------------------ | --------- |
| 1 | Requisitos | `sudo apt update && sudo apt install -y openjdk-17-jdk maven mysql-server git curl` | **Falló** (Falla 1); OK al repetirlo |
| 2 | Requisitos | `java -version` | OK, 17.0.21.1 |
| 3 | Iniciar MySQL | `sudo service mysql start` | OK |
| 4 | Base de datos | `sudo mysql` y `CREATE DATABASE`, `CREATE USER`, `GRANT`, `FLUSH PRIVILEGES` | OK |
| 5 | Configuración local | `cd ~/desarrollo-web/backend` y `cp ...application.properties.example ...application.properties` | **Falló** (Falla 2); OK después de clonar |
| 6 | Clonar | `git clone git@github.com:Peps06/desarrollo-web.git` | No terminó: sin llave SSH, caso que el README ya contempla |
| 7 | Clonar | `git clone https://github.com/Peps06/desarrollo-web.git` y `cd desarrollo-web/backend` | OK |
| 8 | Ejecución | `chmod +x mvnw` y `./mvnw spring-boot:run` | **Falló** (Falla 3); OK tras cargar `esquema.sql` |
| 9 | Verificación de la base | `SHOW TABLES;` y `SELECT COUNT(*) AS usuarios FROM usuario;` | OK, 7 tablas y 2 usuarios |
| 10 | Datos de prueba | (tabla del README) | OK, coinciden |
| 11 | API REST | `curl` de inicio de sesión y de creación de viaje | OK, 200 y 201 |
| 12 | Pruebas | `sudo service mysql start` y `./mvnw test` | OK, 13 pruebas, `BUILD SUCCESS` |

## Fallas encontradas y correcciones

### Falla 1: `mysql-server-8.0` no se configuraba (puerto 3306 ocupado)

La instalación de requisitos terminó con `dpkg: error processing package mysql-server-8.0 (--configure): installed mysql-server-8.0 package post-installation script subprocess returned error exit status 1` (el error completo está en `logs/01-requisitos-instalacion.log`, líneas 734-758 y 1030-1034). En `/var/log/mysql/error.log` aparecía `Can't start server: Bind on TCP/IP port: Address already in use` y `Do you already have another mysqld server running on port: 3306 ?`. En la distribución limpia no había ningún proceso `mysqld`, pero `ss -ltn` mostraba `127.0.0.1:3306` y `127.0.0.1:33060` en escucha (`logs/01b-diagnostico-mysql-server.log`).

La causa es que en WSL 2 todas las distribuciones comparten la red de la misma máquina virtual, y la distribución `Ubuntu` tenía su MySQL en ejecución. Si se hubiera continuado así, la aplicación de la distribución limpia se habría conectado a `localhost:3306`, es decir, a la base de la distribución de desarrollo, y la prueba no tendría sentido.

Se ejecutó solo `wsl --terminate Ubuntu`, que detiene la distribución sin borrar nada. Al repetir el mismo comando del README, `mysql-server-8.0` se configuró y terminó con exit 0. Al README se le añadió una fila en "Problemas comunes" (línea 283) con el mensaje `Errors were encountered while processing: mysql-server-8.0`, su causa y la solución.

### Falla 2: "Configuración local" pedía una carpeta que todavía no existía

El primer comando de "Configuración local", `cd ~/desarrollo-web/backend`, respondió `No such file or directory`, y el `cp` de la plantilla falló por lo mismo (`logs/04-configuracion-local-antes-de-clonar.log`). El README pedía configurar `application.properties` dentro del repositorio antes de explicar cómo clonarlo; la clonación venía después, en "Ejecución".

Después de clonar, la misma sección funcionó sin cambios y `application.properties` no apareció en `git status`. En el README se movió el bloque de clonación, sin cambiar su texto, a una sección nueva, "Clonar el repositorio" (líneas 110-124), antes de "Configuración local".

### Falla 3: Hibernate no creaba las tablas `actividad` ni `gasto`

El primer `./mvnw spring-boot:run` mostró `Started BitacoraViajesApplication` y un segundo después falló con `Table 'bitacora_viajes.actividad' doesn't exist`, `Application run failed` y `BUILD FAILURE` (`logs/07-mvnw-spring-boot-run.log`, líneas 1607-1770). Más arriba en el mismo log (líneas 1003-1084 y 1095-1182), Hibernate intentaba crear `actividad` con `fecha_creacion datetime(6) default CURRENT_TIMESTAMP` y `gasto` con `fecha` igual, y MySQL respondía `Invalid default value for 'fecha_creacion'` y `Invalid default value for 'fecha'`. MySQL 8.0 exige que el valor por defecto tenga la misma precisión que la columna (`CURRENT_TIMESTAMP(6)`), así que esas dos tablas, y por lo tanto los datos de prueba, nunca se crearon. El README afirmaba que la aplicación generaba todas las tablas al arrancar.

Antes de que fallara el arranque alcanzó a insertar los 2 usuarios de prueba. Con un simple reinicio la carga de datos se habría saltado porque "ya hay usuarios" y la base hubiera quedado incompleta, así que la base tiene que estar vacía antes de corregir. Se vació con el comando que el README ya tenía en "Datos de prueba" y ya asi se cargó `database/esquema.sql` con sus 7 tablas y la aplicación arrancó sin errores: `Tomcat started on port 8080` y `Started BitacoraViajesApplication in 5.256 seconds`.

En el README se cambió la frase que decía que las tablas las genera la aplicación (línea 92), se añadió el comando `mysql -u TU_USUARIO -p < ../database/esquema.sql` antes del primer arranque en "Ejecución" (líneas 146-150) y el mismo comando después del `DROP/CREATE DATABASE` de "Datos de prueba" (línea 205). La corrección de fondo está en el código, en `@ColumnDefault("CURRENT_TIMESTAMP")` de `Actividad.java` y `Gasto.java`, y queda fuera de esta HU.

## Observaciones

Estas observaciones no se consideraron fallas del README.

- La imagen de Ubuntu 22.04 para WSL ya trae `git` y `curl`; `java`, `mvn` y `mysql` no existían antes de instalar.
- La línea `Started BitacoraViajesApplication` sola no garantiza que el arranque fue correcto, como se vio en la Falla 3; conviene revisar que después no aparezca `Application run failed`.
- La plantilla `application.properties.example` cita `docs/guia-instalacion-wsl.md`, cuando la ruta real es `documentacion/guia-instalacion-wsl.md`.

## Verificación final

El backend arranca (`Tomcat started on port 8080`, `Started BitacoraViajesApplication`). `SHOW TABLES;` lista las 7 tablas y hay 2 usuarios. Con `ana@example.com` se inició sesión desde el navegador, se quitó y se volvió a registrar un voto (el contador cambió sin recargar, con `fetch`) y se registró un gasto de 350 MXN que actualizó el presupuesto. `./mvnw test`: 13 pruebas, 0 fallos, `BUILD SUCCESS`. Pendiente: captura de la pestaña Network de DevTools al votar.

## Capturas

Las capturas de terminal son sesiones de `bash` en `Ubuntu-22.04` con el usuario `prueba`. Las de fallas y correcciones (`f*` y `c*`) muestran los logs de la ejecución; `hu42-07-git-clone.png` repite el clon en `/tmp`, porque `~/desarrollo-web` ya existía, y en `hu42-01-mvnw-arrancado.png` la línea del comando quedó fuera de la ventana por la cantidad de salida.

| Archivo | Qué muestra |
| ------- | ----------- |
| `hu42-00-wsl-distribuciones.png` | `wsl -l -v` con la distribución de desarrollo y la limpia |
| `hu42-08-versiones.png` | `java -version`, `mvn -version`, `mysql --version` y `git --version` |
| `hu42-07-git-clone.png` | `git clone` por HTTPS con su salida completa |
| `hu42-f1-mysql-server-puerto-3306.png` | Falla 1: error de `dpkg`, errores de `error.log` y puerto 3306 ocupado |
| `hu42-c1-mysql-server-configurado.png` | Corrección 1: reintento con exit 0 y MySQL activo |
| `hu42-f2-configuracion-antes-de-clonar.png` | Falla 2: `cd` y `cp` sin el repositorio clonado |
| `hu42-c2-configuracion-despues-de-clonar.png` | Corrección 2: configuración presente después de clonar |
| `hu42-f3-tabla-actividad-no-creada.png` | Falla 3: `Invalid default value`, `Started` y después `Application run failed` |
| `hu42-c3-esquema-sql-arranque-ok.png` | Corrección 3: base vacía, `esquema.sql` cargado y arranque correcto |
| `hu42-01-mvnw-arrancado.png` | `./mvnw spring-boot:run` con `Tomcat started on port 8080` y `Started BitacoraViajesApplication` |
| `hu42-02-show-tables.png` | Consola de MySQL con `SHOW TABLES;` (7 tablas) y conteo de usuarios |
| `hu42-03b-login-datos-capturados.png` | Formulario de inicio de sesión con `ana@example.com` |
| `hu42-03-login.png` | "Mis viajes" después de iniciar sesión |
| `hu42-03c-detalle-viaje-1.png` | Detalle del viaje 1 (Oaxaca) |
| `hu42-04a-voto-quitado.png` | Voto quitado: contador en 0 |
| `hu42-04-voto.png` | Voto registrado: contador en 1, sin recargar |
| `hu42-05-gasto.png` | Gasto registrado al inicio de la lista |
| `hu42-05b-gasto-presupuesto-actualizado.png` | Presupuesto y balance actualizados después del gasto |
| `hu42-06-mvnw-test.png` | `./mvnw test` con 13 pruebas y `BUILD SUCCESS` |

# Reporte de Uso Documental en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 01 (Citlaly Morales Viveros)

---

# 1. Breve descripción de uso

Consultas a Claude para entender cómo ejecutar el proyecto desde WSL y para resolver errores de arranque. No se pidió generación de código.

---

# 2. Evidencias de uso

| Fecha      | Herramienta | Enlace a prompt                                               | Enlace a respuesta          |
| :--------- | :---------- | :------------------------------------------------------------ | :-------------------------- |
| 20-11-2026 | Claude      | [Consulta 1](#consulta-1-ejecutar-desde-wsl)                  | [Respuesta 1](#respuesta-1) |
| 20-11-2026 | Claude      | [Consulta 2](#consulta-2-error-bad-interpreter)               | [Respuesta 2](#respuesta-2) |
| 20-11-2026 | Claude      | [Consulta 3](#consulta-3-error-access-denied-de-mysql)        | [Respuesta 3](#respuesta-3) |

---

### Consulta 1: Ejecutar desde WSL

> "me recuerdas como se ejecuta desde wsl plis"

Y después, con su `application.properties` pegado:

> "aqui tengo que poner mi contaseña vdd? en cambia esta clave: [su application.properties] Pero realmente no recuerdo cual era, como me meto a mysql en wsl?"

#### Respuesta 1

Se explicó el orden para correr el backend desde WSL, trabajando con el repo clonado dentro de WSL y no en `/mnt/c/...`, porque ahí Spring Boot y Maven van más lentos: entrar a `cd ~/desarrollo-web/backend`, levantar MySQL con `sudo service mysql start` (revisar antes con `status`), la primera vez copiar `application.properties.example` a `application.properties` y revisarlo, y correr `./mvnw spring-boot:run`. Cuando se ve "Started BitacoraViajesApplication", la app está lista; se abre desde Windows en `http://localhost:8081` (Claude primero puso 8080 y después corrigió a 8081, porque el `server.port` de la usuaria es 8081) y se detiene con Ctrl + C. También se avisó de los fallos previstos: Access denied de MySQL, puerto ocupado y `Permission denied` en `mvnw`.

Sobre la contraseña: sí, en `spring.datasource.password` va la contraseña del usuario `bitacora_app`, no la de Windows ni la de root. Si no se recuerda, se puede cambiar entrando como root con `sudo mysql`, revisando con `SELECT user, host FROM mysql.user WHERE user = 'bitacora_app'` y cambiándola con `ALTER USER 'bitacora_app'@'localhost' IDENTIFIED BY '<nueva clave>'` más `FLUSH PRIVILEGES`. Se prueba con `mysql -u bitacora_app -p bitacora_viajes -e "SELECT 1;"` (si responde una tabla con 1, funciona). Se advirtió no subir la clave a git y no pegarla en el chat.

---

### Consulta 2: Error bad interpreter

> "mira me sale un error: root@Laptop-v06:/mnt/c/Users/citli/Desktop/UV/desarrollo-web/backend# ./mvnw spring-boot:run bash: ./mvnw: /bin/sh^M: bad interpreter: No such file or directory"

#### Respuesta 2

El error se debe a que el archivo `mvnw` tiene saltos de línea de Windows (CRLF), y Linux no los entiende. Pasa porque el repo se guardó desde Windows. Se arregla convirtiéndolo a formato Linux (LF) con `sed -i 's/\r$//' mvnw` y dando permiso de ejecución con `chmod +x mvnw`; en `/mnt/c` a veces el permiso no se conserva, y si sale `Permission denied` se usa `bash mvnw spring-boot:run`. Se confirma con `file mvnw`, que debe decir "ASCII text executable" o similar, sin CRLF. Se recomendó trabajar desde la carpeta de WSL (clonar con `git clone https://github.com/Peps06/desarrollo-web.git` y después copiar su `application.properties` con su clave, porque no está en git) y no hacer commit del cambio en `mvnw` sin avisar al equipo; lo ideal es que el equipo agregue un `.gitattributes` con `mvnw text eol=lf`.

---

### Consulta 3: Error Access denied de MySQL

> "oh no, errores :c"

Con el registro del arranque, que decía `Access denied for user 'bitacora_app'@'localhost' (using password: YES)`.

#### Respuesta 3

Se explicó que el proyecto compiló y arrancó, y que el error está solo en la conexión a MySQL: Spring sí intentó entrar, pero MySQL rechazó a `bitacora_app` con la clave de `application.properties`, y las demás líneas eran consecuencia porque Hibernate no pudo conectarse. Causas más comunes: la clave del archivo no coincide con la de MySQL, el archivo todavía tiene `CAMBIA_ESTA_CLAVE`, o el usuario no existe con ese host. Pasos: probar la clave directamente con `mysql -u bitacora_app -p -h 127.0.0.1 bitacora_viajes -e "SELECT 1;"`; si falla, restablecerla como root con `ALTER USER`; y revisar el archivo con `grep -n "password\|username"`, confirmando la clave real, sin espacios alrededor del `=`, sin `CAMBIA_ESTA_CLAVE` y sin comillas.

Seguimiento: la usuaria comprobó que `SELECT 1` sí funcionó y que el usuario existe con `caching_sha2_password`, así que la clave de MySQL sí sirve y el problema está en la clave que lee Spring. Claude pidió comparar el archivo que realmente lee la app (`src/main/resources/application.properties` contra `target/classes/application.properties`, porque Spring lee la copia de `target/classes`), y descartar caracteres invisibles o espacios con `grep -n "password" ... | cat -A` y `sed -i 's/\r$//'` si hay `^M$`. Después la usuaria revisó con `sudo ss -ltnp | grep 3306` y solo escuchaba `mysqld` con PID 307, la instancia de WSL, así que el conflicto de puerto quedó descartado: el problema seguía siendo la clave (o cómo Spring la lee del archivo), no el puerto ni un usuario inexistente.

---

## Observaciones adicionales

- Consulta 1: aprendí el orden para correr el proyecto desde WSL (clonado dentro de WSL, MySQL levantado, `application.properties` con mi clave y `./mvnw spring-boot:run`) y que mi app abre en el puerto 8081.
- Consulta 2: aprendí que el error `bad interpreter` es por los saltos de línea de Windows en `mvnw` y que se arregla con `sed` y `chmod +x`.
- Consulta 3: aprendí a probar el acceso a MySQL directo desde WSL y a descartar el puerto con `ss`; la causa final sigue por confirmar (ver arriba).

# Reporte de Uso Documental en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 02 (Alfonso Mendoza Vásquez)

---

# 1. Breve descripción de uso

Consultas para levantar el servidor local y poder hacer las pruebas manuales de la votación: en qué puerto corre la aplicación, por qué falló la conexión a MySQL al arrancar y con qué usuarios entrar a probar.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 20-11-2026 | Claude | [Consulta 1: Servidor y puerto para las pruebas manuales](#consulta-1-servidor-y-puerto-para-las-pruebas-manuales) | [Respuesta 1](#respuesta-1) |
| 20-11-2026 | Claude | [Consulta 2: Error Access denied al arrancar el servidor](#consulta-2-error-access-denied-al-arrancar-el-servidor) | [Respuesta 2](#respuesta-2) |
| 20-11-2026 | Claude | [Consulta 3: Credenciales para ingresar a probar](#consulta-3-credenciales-para-ingresar-a-probar) | [Respuesta 3](#respuesta-3) |

---

### Consulta 1: Servidor y puerto para las pruebas manuales
> "No me enteré, hubo un cambio al servidor en el puerto? Donde puedo hacer las pruebas manuales?"

#### Respuesta 1
Se revisó el proyecto: la aplicación está configurada en el puerto 8080 (`server.port` de `application.properties`) y en ese momento no había nada escuchando ahí; solo MySQL estaba activo en el 3306. Se indicó arrancarla desde la carpeta `backend` con `./mvnw spring-boot:run` y abrir `http://localhost:8080`. También se explicó que `frontend/js` se copia a `target/classes/static` al compilar, así que después de cambiar un archivo hay que detener y volver a arrancar el servidor y recargar con Ctrl+F5. Se advirtió que las pruebas de votar no se podían hacer completas hasta que el botón de voto estuviera en las tarjetas del detalle del viaje.

---

### Consulta 2: Error Access denied al arrancar el servidor
> "Falla correr el servidor, ya entré a backend y corrí el comando que me diste, pero me sale el siguiente error: [salida de `./mvnw spring-boot:run` con `Access denied for user 'bitacora_app'@'localhost'`]. Explícame qué pasó, por qué pasó y cómo arreglarlo"

#### Respuesta 2
La aplicación sí compiló y arrancó Tomcat, pero MySQL rechazó el usuario `bitacora_app`. Los demás errores (`Unable to determine Dialect`, `BUILD FAILURE`) eran consecuencia de no poder conectarse. La causa era que existen dos MySQL en el equipo: el de WSL, donde estaban el usuario y la base creados antes, y un `mysqld` de Windows que ocupaba el puerto 3306; al correr `./mvnw` desde PowerShell la aplicación se conectó al de Windows, donde ese usuario no existe o tiene otra contraseña. Se propusieron dos soluciones: arrancar desde WSL (`sudo service mysql start` y luego `./mvnw spring-boot:run`), que es la recomendada porque ahí ya están los datos, o crear el usuario y cargar `database/esquema.sql` en el MySQL de Windows, con la base vacía. No se modificó ningún archivo, porque `application.properties` está en `.gitignore`.

---

### Consulta 3: Credenciales para ingresar a probar
> "Cuáles son las credenciales para ingresar y hacer las pruebas?"

#### Respuesta 3
Según el README, la aplicación carga datos de prueba la primera vez que arranca con la base sin usuarios: `ana@example.com` (organizadora) y `luis@example.com` (participante), ambos con la contraseña `Password123`, y un viaje a Oaxaca con dos actividades con votos. Se recomendó usar una ventana normal y otra de incógnito para tener dos sesiones a la vez. Se aclaró que si la base ya tenía usuarios, los datos de prueba no se vuelven a cargar y habría que registrarse en `/registro`.

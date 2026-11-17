# Reporte de Uso Documental en el Proyecto

**Fecha:** 16-11-2026
**Número de reporte:** 02 (Diego Rafael Jiménez Trujano)

---

# 1. Breve descripción de uso

Consultas para decidir el alcance del entregable 3: qué se podía hacer sin depender de las historias de votar y de presupuesto, si el trabajo realizado invadía historias de otros integrantes, y cómo resolver los problemas de conexión a MySQL y de puerto ocupado al probar.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 16-11-2026 | Claude | [Consulta 1: Dependencias de las historias antes de programar](#consulta-1-dependencias-de-las-historias-antes-de-programar) | [Respuesta 1](#respuesta-1) |
| 16-11-2026 | Claude | [Consulta 2: Trabajo de otras historias](#consulta-2-trabajo-de-otras-historias) | [Respuesta 2](#respuesta-2) |
| 16-11-2026 | Claude | [Consulta 3: Dos servidores MySQL y puerto ocupado](#consulta-3-dos-servidores-mysql-y-puerto-ocupado) | [Respuesta 3](#respuesta-3) |

---

### Consulta 1: Dependencias de las historias antes de programar
> "Estamos en la entrega 3, todas las tareas anteriores ya están hechas menos las HU 14 y 16 en entregable 2. Quiero que me ayudes a hacer mi entregable 3, antes de hacer algo de código, quiero que me digas qué es lo que se puede hacer que no se dependan de esas HU."

#### Respuesta 1
Se revisaron las historias de usuario, el catálogo de endpoints y el código del repositorio. La historia de editar y eliminar actividades depende de la de votar solo por la regla de borrar los votos al eliminar una actividad, y esa regla ya estaba resuelta en el esquema (`ON DELETE CASCADE` en la tabla `voto`) y en la entidad `Voto`. La historia de presupuesto no tiene relación con editar o eliminar actividades. Además, el código de votar y de presupuesto ya aparecía en `main`. Por eso se podía hacer el `PUT` y el `DELETE` de actividades, los filtros y las pruebas sin esperar esas historias.

---

### Consulta 2: Trabajo de otras historias
> "¿Hicimos algo de alguien más?" y "en el excel decía que una de nuestras HU dependía de una de Alfonso, ¿no hicimos la de Alfonso?"

#### Respuesta 2
No se implementó ninguna historia ajena. Se agregaron los parámetros opcionales `destino` y `rol` al `GET /api/viajes` y una consulta nueva a `ParticipanteViajeRepository`, y tres consultas nuevas a `ActividadRepository`, sin quitar nada de lo existente. Los endpoints de votar y quitar voto de Alfonso no se tocaron; el borrado de los votos de una actividad se hizo con una consulta propia en `ActividadRepository`. Se recomendó avisar a Stephanie de los cambios en sus archivos de viajes para evitar conflictos al hacer merge.

---

### Consulta 3: Dos servidores MySQL y puerto ocupado
> "¿Me tengo que meter a WSL?" (junto con el log de arranque con `Access denied for user 'bitacora_app'@'localhost'` y la respuesta `404` de Jetty al iniciar sesión)

#### Respuesta 3
El `Access denied` se debía a que había dos servidores MySQL: el servicio `MySQL80` de Windows, en el puerto 3306, al que se conectaba la aplicación, y el de WSL, donde sí existía el usuario `bitacora_app`. Se indicó entrar a WSL como `root` con `wsl -u root`, que no pide contraseña, y correr la aplicación desde WSL para que `localhost` apunte al MySQL correcto. El `404` con página de Jetty no era de la aplicación (que usa Tomcat) sino de otro programa que ocupaba el puerto 8080, así que se arrancó la aplicación en el puerto 8081 con `-Dspring-boot.run.arguments=--server.port=8081`, sin modificar archivos del proyecto.

---

## Observaciones adicionales

Estas consultas sirvieron para delimitar el alcance del entregable antes de programar y para destrabar el entorno de pruebas. Las decisiones técnicas de implementación (filtros opcionales en la consulta, borrado de votos en la misma transacción y permisos de autor u organizador) quedaron en el prompt del reporte de IA de desarrollo 02.

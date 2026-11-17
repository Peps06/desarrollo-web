# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 16-11-2026
**Número de reporte:** 02 (Diego Rafael Jiménez Trujano - Editar y eliminar actividades y filtros de búsqueda)

---

# 1. Breve descripción de uso

Apoyo en la codificación del backend para **Editar actividad (`PUT /api/actividades/{id}`)**, **Eliminar actividad (`DELETE /api/actividades/{id}`)** y los **filtros de búsqueda** (`GET /api/viajes/{id}/actividades?dia=&costoMax=` y `GET /api/viajes?destino=&rol=`), siguiendo la arquitectura de 3 capas del sistema (`Controller`, `Service`, `Repository` y `DTO`) y reutilizando `ActividadRequest`, `ActividadResponse`, `buscarParticipacion`, `ErrorNegocioException` y el formato `ErrorResponse`.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Sonnet 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `e6f1dc5` (reportes IA 2) |
| **Código posterior** | Commit `d58e19e` (Editar, eliminar y filtrar actividades y viajes) |
| **Diferencias entre versiones** | Ver salida de `git diff --numstat e6f1dc5 d58e19e` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver pruebas](#pruebas) con PowerShell (`Invoke-WebRequest`) y sesión por cookie, ejecutadas por mí |
| **Código de uso** | Generación / Depuración / Explicación |
| **Reflexión del estudiante** | Entendí que si la validación de los datos se hace con `@Valid` en el controlador, un cuerpo inválido responde 400 antes de saber si la actividad existe o si el usuario tiene permiso. Para respetar el orden 404, 403 y 400, el controlador del `PUT` recibe el cuerpo sin `@Valid` y el servicio valida después de comprobar la actividad y el permiso. También entendí que los filtros opcionales se resuelven en la consulta con `(:parametro IS NULL OR condición)`, así la base de datos filtra y no se recorre una lista completa en Java. Por último, al eliminar una actividad se borran sus votos de forma explícita en la misma transacción, sin depender solo del `ON DELETE CASCADE` del esquema. |

---

### Prompt original
> "Actúa como desarrollador backend con experiencia en Spring Boot 3, Spring Data JPA y Spring Security. Trabajas en el repositorio de la 'Bitácora de viajes colaborativa' (Java 17, MySQL, arquitectura de 3 capas: Controller, Service, Repository y DTO). Revisa el código existente, en particular `ActividadController`, `ActividadService`, `ViajeDetalleService`, `ViajeController`, `ViajeService` y los repositorios, y agrega tres funcionalidades reutilizando lo que ya hay:
>
> 1. **Editar actividad** (`PUT /api/actividades/{id}`): recibe los mismos campos y aplica las mismas validaciones que al proponer una actividad (título y hora obligatorios, día dentro de las fechas del viaje, costo no negativo). Responde `200` con la actividad actualizada, incluyendo su total de votos y si el usuario en sesión ya votó.
> 2. **Eliminar actividad** (`DELETE /api/actividades/{id}`): responde `204`. Sus votos se eliminan en la misma transacción y la actividad deja de aparecer en el detalle del viaje.
> 3. **Filtros de búsqueda**:
>    - `GET /api/viajes/{id}/actividades` con los parámetros opcionales y combinables `dia` (`yyyy-MM-dd`) y `costoMax`. Si el endpoint no existe, créalo.
>    - `GET /api/viajes` con los parámetros opcionales y combinables `destino` (coincidencia parcial, sin distinguir mayúsculas) y `rol` (`organizador` o `participante`), sin cambiar la respuesta base actual.
>
> Reglas: solo el autor de la actividad o el organizador del viaje pueden editarla o eliminarla; cualquier otro participante recibe `403`. El usuario siempre se toma de la sesión. El orden de las respuestas de error es `404` si el recurso no existe, `403` si no tiene permiso y `400` en validaciones. Un `rol` inválido o una fecha mal formada responden `400` con mensaje; un filtro sin resultados responde `200` con lista vacía. Los filtros se aplican en la consulta del repositorio (`@Query` con parámetros opcionales o `Specification`), nunca filtrando en Java sobre toda la lista. Usa DTOs en lugar de entidades y evita el problema N+1.
>
> Restricciones: reutiliza lo que ya existe (`ActividadRequest`, `ActividadResponse`, `buscarParticipacion`, `ErrorNegocioException` y el formato `ErrorResponse`), no modifiques archivos de otros integrantes salvo para agregar un parámetro opcional al `GET /api/viajes` y su consulta, no agregues archivos CSS y crea archivos nuevos solo si es indispensable. Los comentarios deben ser solo documentación breve de la funcionalidad, en español y sin mencionar historias de usuario. Entrega el código completo de cada archivo modificado y comandos `cURL` para probar cada filtro solo y combinado, editar una actividad propia, editar una ajena (403) y eliminar una con votos."

### Respuesta de IA
La herramienta modificó seis archivos existentes, sin crear archivos nuevos:

- **`ActividadController`**: agregó `GET /api/viajes/{viajeId}/actividades` (con `dia` y `costoMax` opcionales), `PUT /api/actividades/{actividadId}` (responde 200) y `DELETE /api/actividades/{actividadId}` (responde 204). El `PUT` recibe el cuerpo sin `@Valid` para que la validación ocurra en el servicio.
- **`ActividadService`**: agregó `listar`, `editar` y `eliminar`. Buscar la actividad (404) y verificar el permiso (403, solo autor u organizador, reutilizando `buscarParticipacion`) se hacen antes de validar los datos (400). La validación usa el `Validator` de Jakarta sobre `ActividadRequest`, de modo que aplica las mismas reglas que al proponer una actividad. Las reglas del día dentro del rango del viaje y la normalización de los campos se extrajeron de `crear` a métodos privados que ahora comparten `crear` y `editar`. Los filtros `dia` y `costoMax` se reciben como texto y se convierten después de comprobar el viaje, para responder 400 con mensaje si la fecha o el número son inválidos. La edición devuelve `totalVotos` y `votadaPorMi`, y el listado obtiene los conteos de todo el viaje con una consulta agrupada para evitar el problema N+1.
- **`ActividadRepository`**: agregó `buscarConViajeYAutor` (con `JOIN FETCH`), `filtrarPorViaje` (con `(:dia IS NULL OR a.dia = :dia)` y `(:costoMax IS NULL OR a.costoEstimado <= :costoMax)`) y `eliminarVotos` (borrado JPQL de los votos de la actividad, que se ejecuta antes de borrarla dentro de la misma transacción).
- **`ParticipanteViajeRepository`**: agregó `buscarFiltrados`, con `destino` como patrón `LIKE` en minúsculas con carácter de escape y `rol` opcional.
- **`ViajeService`**: `listarMisViajes` recibe `destino` y `rol`, convierte el rol (400 si es inválido) y arma el patrón de búsqueda escapando los comodines.
- **`ViajeController`**: `GET /api/viajes` acepta `destino` y `rol` opcionales sin cambiar la respuesta base.

La herramienta no pudo compilar ni ejecutar la aplicación; la compilación y todas las pruebas las hice yo en mi equipo.

### Diferencias entre versiones
```text
30	0	backend/src/main/java/com/bitacora/viajes/controller/ActividadController.java
8	4	backend/src/main/java/com/bitacora/viajes/controller/ViajeController.java
23	0	backend/src/main/java/com/bitacora/viajes/repository/ActividadRepository.java
15	1	backend/src/main/java/com/bitacora/viajes/repository/ParticipanteViajeRepository.java
152	6	backend/src/main/java/com/bitacora/viajes/service/ActividadService.java
31	3	backend/src/main/java/com/bitacora/viajes/service/ViajeService.java
```

---

### Pruebas
Pruebas hechas por mí con la aplicación corriendo desde WSL en el puerto 8081 y los datos de `DataLoader` (viaje 1 de Oaxaca del 2026-12-20 al 2026-12-27; Ana es organizadora y Luis participante; actividad 1 de Ana con 2 votos, actividad 2 de Luis con 1 voto y actividad 3 de Ana sin votos). Se usó PowerShell con una función `Llamar` que imprime el código HTTP y el cuerpo. Los acentos aparecen como `LÃ³pez` solo por cómo PowerShell 5.1 muestra el texto. En los códigos 400, 403 y 404 la función solo imprime el código; el mensaje de un 400 se muestra al final.

Inicio de sesión:

```
PS> $B = "http://localhost:8081"
PS> $null = Invoke-RestMethod -Method Post -Uri "$B/api/auth/login" -ContentType "application/json" -Body '{"correo":"ana@example.com","contrasena":"Password123"}' -SessionVariable ana
PS> $null = Invoke-RestMethod -Method Post -Uri "$B/api/auth/login" -ContentType "application/json" -Body '{"correo":"luis@example.com","contrasena":"Password123"}' -SessionVariable luis
```

Estado inicial del viaje:

```
PS> Llamar GET "/api/viajes/1" $ana
200 {"id":1,"destino":"Oaxaca, Oaxaca","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":15000.00,"monedaBase":"MXN","rol":"ORGANIZADOR","totalGastado":5025.00,"participantes":[{"usuarioId":1,"nombre":"Ana LÃ³pez","rol":"ORGANIZADOR"},{"usuarioId":2,"nombre":"Luis PÃ©rez","rol":"PARTICIPANTE"}],"dias":[{"fecha":"2026-12-20","actividades":[]},{"fecha":"2026-12-21","actividades":[{"id":1,"viajeId":1,"titulo":"Visita a Hierve el Agua","descripcion":"Cascadas petrificadas y albercas naturales.","dia":"2026-12-21","hora":"09:00","costoEstimado":350.00,"autor":{"id":1,"nombre":"Ana LÃ³pez"},"totalVotos":2,"votadaPorMi":true}]},{"fecha":"2026-12-22","actividades":[{"id":2,"viajeId":1,"titulo":"Cena en el Mercado 20 de Noviembre","descripcion":"Probar tlayudas y mole.","dia":"2026-12-22","hora":"20:00","costoEstimado":400.00,"autor":{"id":2,"nombre":"Luis PÃ©rez"},"totalVotos":1,"votadaPorMi":true}]},{"fecha":"2026-12-23","actividades":[{"id":3,"viajeId":1,"titulo":"Visita a Monte AlbÃ¡n","descripcion":"Recorrido de 3 h","dia":"2026-12-23","hora":"09:00","costoEstimado":150.00,"autor":{"id":1,"nombre":"Ana LÃ³pez"},"totalVotos":0,"votadaPorMi":false}]},{"fecha":"2026-12-24","actividades":[]},{"fecha":"2026-12-25","actividades":[]},{"fecha":"2026-12-26","actividades":[]},{"fecha":"2026-12-27","actividades":[]}]}
```

Filtros de actividades, solos y combinados:

```
PS> Llamar GET "/api/viajes/1/actividades?dia=2026-12-21" $ana
200 [{"id":1,"viajeId":1,"titulo":"Visita a Hierve el Agua","descripcion":"Cascadas petrificadas y albercas naturales.","dia":"2026-12-21","hora":"09:00","costoEstimado":350.00,"autor":{"id":1,"nombre":"Ana LÃ³pez"},"totalVotos":2,"votadaPorMi":true}]

PS> Llamar GET "/api/viajes/1/actividades?costoMax=360" $ana
200 [{"id":1,"viajeId":1,"titulo":"Visita a Hierve el Agua","descripcion":"Cascadas petrificadas y albercas naturales.","dia":"2026-12-21","hora":"09:00","costoEstimado":350.00,"autor":{"id":1,"nombre":"Ana LÃ³pez"},"totalVotos":2,"votadaPorMi":true},{"id":3,"viajeId":1,"titulo":"Visita a Monte AlbÃ¡n","descripcion":"Recorrido de 3 h","dia":"2026-12-23","hora":"09:00","costoEstimado":150.00,"autor":{"id":1,"nombre":"Ana LÃ³pez"},"totalVotos":0,"votadaPorMi":false}]

PS> Llamar GET "/api/viajes/1/actividades?dia=2026-12-22&costoMax=500" $ana
200 [{"id":2,"viajeId":1,"titulo":"Cena en el Mercado 20 de Noviembre","descripcion":"Probar tlayudas y mole.","dia":"2026-12-22","hora":"20:00","costoEstimado":400.00,"autor":{"id":2,"nombre":"Luis PÃ©rez"},"totalVotos":1,"votadaPorMi":true}]

PS> Llamar GET "/api/viajes/1/actividades?dia=2026-12-22&costoMax=100" $ana
200 []

PS> Llamar GET "/api/viajes/1/actividades?dia=21-12-2026" $ana
400

PS> Llamar GET "/api/viajes/1/actividades?costoMax=abc" $ana
400
```

Filtros de viajes, solos y combinados:

```
PS> Llamar GET "/api/viajes?destino=OAX" $ana
200 [{"id":1,"destino":"Oaxaca, Oaxaca","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":15000.00,"monedaBase":"MXN","rol":"ORGANIZADOR"}]

PS> Llamar GET "/api/viajes?rol=organizador" $ana
200 [{"id":1,"destino":"Oaxaca, Oaxaca","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":15000.00,"monedaBase":"MXN","rol":"ORGANIZADOR"}]

PS> Llamar GET "/api/viajes?destino=oax&rol=organizador" $luis
200 []

PS> Llamar GET "/api/viajes?destino=xyz" $ana
200 []

PS> Llamar GET "/api/viajes?rol=jefe" $ana
400
```

Mensaje del 400 por rol inválido:

```
PS> try { Invoke-WebRequest -UseBasicParsing -Uri "$B/api/viajes?rol=jefe" -WebSession $ana } catch { (New-Object IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd() }
{"codigo":400,"mensaje":"El rol debe ser organizador o participante"}
```

Editar actividades:

```
PS> Llamar PUT "/api/actividades/2" $luis '{"titulo":"Cena en el mercado","descripcion":"Tlayudas","dia":"2026-12-22","hora":"21:00","costoEstimado":450}'
200 {"id":2,"viajeId":1,"titulo":"Cena en el mercado","descripcion":"Tlayudas","dia":"2026-12-22","hora":"21:00","costoEstimado":450.00,"autor":{"id":2,"nombre":"Luis PÃ©rez"},"totalVotos":1,"votadaPorMi":false}

PS> Llamar PUT "/api/actividades/1" $luis '{"titulo":"X","dia":"2026-12-21","hora":"10:00"}'
403

PS> Llamar PUT "/api/actividades/2" $ana '{"titulo":"Cena","dia":"2026-12-22","hora":"20:30"}'
200 {"id":2,"viajeId":1,"titulo":"Cena","descripcion":null,"dia":"2026-12-22","hora":"20:30","costoEstimado":0.00,"autor":{"id":2,"nombre":"Luis PÃ©rez"},"totalVotos":1,"votadaPorMi":true}

PS> Llamar PUT "/api/actividades/999" $ana '{"titulo":"X","dia":"2026-12-21","hora":"10:00"}'
404

PS> Llamar PUT "/api/actividades/1" $ana '{"titulo":"X","dia":"2027-01-01","hora":"10:00"}'
400

PS> Llamar PUT "/api/actividades/1" $ana '{"dia":"2026-12-21","hora":"10:00"}'
400
```

Eliminar una actividad con votos (la actividad 1 tenía 2 votos) y comprobar en MySQL:

```
PS> Llamar DELETE "/api/actividades/1" $luis
403

PS> Llamar DELETE "/api/actividades/1" $ana
204

PS> Llamar GET "/api/viajes/1" $ana
200 {"id":1,"destino":"Oaxaca, Oaxaca",...,"dias":[{"fecha":"2026-12-20","actividades":[]},{"fecha":"2026-12-21","actividades":[]},{"fecha":"2026-12-22","actividades":[{"id":2,"viajeId":1,"titulo":"Cena","descripcion":null,...,"totalVotos":1,"votadaPorMi":true}]},{"fecha":"2026-12-23","actividades":[{"id":3,"viajeId":1,"titulo":"Visita a Monte AlbÃ¡n",...,"totalVotos":0,"votadaPorMi":false}]},...]}
(salida recortada: el día 2026-12-21 ya no tiene la actividad)

PS> wsl -u root mysql -e "SELECT * FROM bitacora_viajes.voto WHERE actividad_id=1;"
(sin filas: los votos de la actividad 1 ya no existen en la tabla voto)
```

No se incluyó una prueba de 401 sin sesión para este entregable.

---

## Observaciones adicionales

El `PUT` reemplaza todos los campos de la actividad: si se envía sin `descripcion` ni `costoEstimado`, la descripción queda en `null` y el costo en `0.00`, como se ve en la prueba del organizador. Un JSON mal formado (por ejemplo una hora inválida) sigue respondiendo 400 antes de comprobar si la actividad existe, porque ocurre al leer el cuerpo. La consulta anterior `buscarPorUsuarioYEstadoViaje` de `ParticipanteViajeRepository` se conservó, pero `listarMisViajes` ahora usa `buscarFiltrados`. Antes de programar se revisó que las historias de votar (`VotoService`) y consultar presupuesto ya estaban en `main`; no se modificaron, y el borrado de votos de la actividad se resolvió con una consulta propia en `ActividadRepository`.

Problemas durante las pruebas: la aplicación no arrancaba con `Access denied for user 'bitacora_app'` porque en el equipo había dos servidores MySQL (el servicio `MySQL80` de Windows y el de WSL) escuchando en el puerto 3306, y la aplicación en Windows se conectaba al que no tenía el usuario. Se resolvió corriendo la aplicación desde WSL. Además, el puerto 8080 estaba ocupado por otro programa (Jetty), por lo que la aplicación se arrancó en el puerto 8081 con `-Dspring-boot.run.arguments=--server.port=8081`.

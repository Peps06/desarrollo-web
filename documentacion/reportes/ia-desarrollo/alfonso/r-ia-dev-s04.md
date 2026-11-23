# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 16-11-2026
**Número de reporte:** 04 (Alfonso Mendoza Vásquez - Actividad más votada por día)

---

# 1. Breve descripción de uso

Implementación de `ActividadGanadoraService`, que elige la actividad más votada de cada día del viaje. Desempate: más votos → menor `costoEstimado` → creada primero (`fechaCreacion`). Si el día no tiene votos o hay empate total, no hay ganadora (`null`). El resultado se integra al detalle del viaje (`GET /api/viajes/{id}`, HU-12) como el campo `actividadMasVotada` de cada día, reutilizando los conteos de `VotoRepository.contarPorViaje` (una sola consulta para todo el viaje).

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude(Anthropic) |
| **Modelo/temperatura** | Claude Sonnet 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `7b106317903d6e29a20e93d35448e7520f315114` (HU-21: balance de deudas del grupo) |
| **Código posterior** | Commit `69055cd68932db10811078432731a43611e07ec7` (HU-22: actividad más votada por día) |
| **Diferencias entre versiones** | [Ver diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Aprendí a resolver un empate con varios criterios en orden: solo se pasa al siguiente criterio si el anterior empató. Recorrer las actividades llevando una ganadora provisional es más fácil de seguir que ordenar toda la lista. También aprendí a integrar mi parte al código de otro compañero tocando lo mínimo y reutilizando la consulta de votos que ya existía, en lugar de hacer una consulta por cada actividad. En las pruebas con cURL vi un caso real de empate total: dos actividades creadas en el mismo segundo quedaron con la misma `fechaCreacion`, porque la columna `DATETIME` guarda solo segundos. |

---

### Prompt original
> Ahora HU-22: Actividad más votada por día. Los archivos ActividadGanadoraService.java, ActividadGanadoraServiceTest.java y ActividadMasVotadaResponse.java existen pero están vacíos.
>
> Requisitos:
> - ActividadGanadoraService calcula, para cada día del viaje, la actividad con más votos.
> - Desempate: gana la de menor costoEstimado; si siguen empatadas, la que se creó primero (Actividad.fechaCreacion).
> - Si el día no tiene votos, o si hay empate total (mismos votos, mismo costo y misma fechaCreacion), no hay ganadora: el campo va en null.
> - ActividadMasVotadaResponse es un record con {id, titulo, totalVotos}.
> - Se integra al detalle del viaje (HU-12, código de Diego): DiaResponse hoy es record DiaResponse(LocalDate fecha, List<ActividadResponse> actividades). Agrégale un tercer campo actividadMasVotada y modifica ViajeDetalleService.obtenerDetalle SOLO donde se arma cada DiaResponse (alrededor de la línea 72). No cambies nada más del código de Diego y dime exactamente qué líneas tocaste.
> - Para los conteos reutiliza lo que ya existe: VotoRepository.contarPorViaje(viajeId, usuarioId) devuelve el total de votos por actividad. No hagas una consulta por cada actividad.
> - Deja un método público que reciba la lista de actividades de un día y un Map<Integer, Long> con los votos, sin usar la base de datos, para poder probarlo con JUnit.
>
> Pruebas JUnit en ActividadGanadoraServiceTest:
> 1. Sin votos: devuelve null.
> 2. Ganadora clara por número de votos.
> 3. Empate en votos que se resuelve por menor costo.
> 4. Empate en votos y costo que se resuelve por la creada primero.
> 5. Empate total: devuelve null.
>
> Genera el código completo de cada archivo, el cURL de GET /api/viajes/{id} donde se vea actividadMasVotada y una explicación simple de cómo funciona el desempate.

Instrucción general de estilo dada en conversaciones anteriores:
> "todo el codigo que consideres añadir o modificar debe ser: Limpio, sencillo, y entendible. Para que yo y cualquiera lo entienda completamente"

### Respuesta de IA
Se generaron tres archivos y se modificaron dos:
- `ActividadMasVotadaResponse`: record `(id, titulo, totalVotos)`.
- `ActividadGanadoraService`: no consulta la base de datos. El método público `elegirGanadora(actividadesDelDia, Map<Integer, Long> votos)` recorre las actividades del día con una "ganadora provisional": las que no tienen votos no compiten, la que le gana a la provisional toma su lugar y, si empata en los tres criterios, se marca empate total. Un método privado `comparar` aplica el orden de desempate (votos, costo, fecha de creación). El método `calcularPorDia(actividades, conteos)` convierte los conteos de `contarPorViaje` a `Map<Integer, Long>`, separa las actividades por día y llama a `elegirGanadora` en cada uno.
- `ActividadGanadoraServiceTest`: 5 pruebas JUnit de `elegirGanadora`. Como `Actividad` no tiene `setId` (lo genera MySQL), el id se asigna con `ReflectionTestUtils` de Spring, sin modificar el modelo.
- `DiaResponse` (código de Diego): tercer campo `actividadMasVotada`.
- `ViajeDetalleService` (código de Diego): la IA indicó que no era posible tocar solo la línea 72, y explicó por qué cada cambio extra era necesario:

| Líneas (versión nueva) | Cambio | Motivo |
| :--- | :--- | :--- |
| 3 | `import ...ActividadMasVotadaResponse;` | El mapa de ganadoras usa ese tipo |
| 38, 44-45, 51 | Campo, parámetro del constructor y asignación de `actividadGanadoraService` | Spring inyecta el servicio por constructor |
| 70-71 | La lista de `buscarPorViaje(viajeId)` se guarda en `List<Actividad> actividades` antes del stream | El desempate necesita `fechaCreacion`, que `ActividadResponse` no trae; así se reutiliza la lista sin otra consulta |
| 76-77 | Se calcula el mapa `ganadoras` | Lo pedido |
| 81-82 | `new DiaResponse(...)` recibe `ganadoras.get(fecha)` | Lo pedido |

Además se dio el cURL del detalle y una explicación paso a paso del desempate.

### Diferencias entre versiones
```text
8	1	backend/src/main/java/com/bitacora/viajes/dto/ActividadMasVotadaResponse.java
3	1	backend/src/main/java/com/bitacora/viajes/dto/DiaResponse.java
132	1	backend/src/main/java/com/bitacora/viajes/service/ActividadGanadoraService.java
12	3	backend/src/main/java/com/bitacora/viajes/service/ViajeDetalleService.java
116	1	backend/src/test/java/com/bitacora/viajes/service/ActividadGanadoraServiceTest.java
```

---

### Pruebas

**Pruebas unitarias (JUnit)**, ejecutadas en WSL:
```text
$ cd backend && ./mvnw test -Dtest=ActividadGanadoraServiceTest
-------------------------------------------------------------------------------
Test set: com.bitacora.viajes.service.ActividadGanadoraServiceTest
-------------------------------------------------------------------------------
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.800 s -- in com.bitacora.viajes.service.ActividadGanadoraServiceTest
```

| Prueba | Qué comprueba |
| :--- | :--- |
| `sinVotosNoHayGanadora` | Mapa de votos vacío → `null` |
| `ganaLaDeMasVotos` | 1, 2 y 4 votos → gana Tour (4 votos) |
| `empateEnVotosGanaLaMasBarata` | 3 y 3 votos, costos 200 y 50 → gana Playa (50) |
| `empateEnVotosYCostoGanaLaCreadaPrimero` | Mismos votos y costo; la creada primero va segunda en la lista y aun así gana (no depende del orden) |
| `empateTotalNoHayGanadora` | Dos actividades iguales en votos, costo y fecha → `null` |

**Pruebas con cURL**, ejecutadas desde Claude Code en WSL contra el servidor local (`./mvnw spring-boot:run`), con los datos de prueba del `DataLoader` (viaje 1 del 20 al 27 de diciembre; Hierve el Agua el 21-dic con 2 votos y la Cena el 22-dic con 1 voto). Las sesiones de Luis, Ana y Pepe se abrieron con `POST /api/auth/login` igual que en el reporte 03. Para ver solo el campo nuevo de cada día se usa este script de Python (`/tmp/dias.py`):
```python
import json, sys
for d in json.load(sys.stdin)["dias"]:
    print(d["fecha"], json.dumps(d["actividadMasVotada"], ensure_ascii=False))
```

```text
# 1. Detalle del viaje con los datos de prueba → 200 (ganadora clara el 21-dic)
$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/1
{"id":1,"destino":"Oaxaca, Oaxaca","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":15000.00,"monedaBase":"MXN","rol":"PARTICIPANTE","totalGastado":5025.00,"participantes":[{"usuarioId":1,"nombre":"Ana López","rol":"ORGANIZADOR"},{"usuarioId":2,"nombre":"Luis Pérez","rol":"PARTICIPANTE"}],"dias":[{"fecha":"2026-12-20","actividades":[],"actividadMasVotada":null},{"fecha":"2026-12-21","actividades":[{"id":1,"viajeId":1,"titulo":"Visita a Hierve el Agua","descripcion":"Cascadas petrificadas y albercas naturales.","dia":"2026-12-21","hora":"09:00","costoEstimado":350.00,"autor":{"id":1,"nombre":"Ana López"},"totalVotos":2,"votadaPorMi":true}],"actividadMasVotada":{"id":1,"titulo":"Visita a Hierve el Agua","totalVotos":2}},{"fecha":"2026-12-22","actividades":[{"id":2,"viajeId":1,"titulo":"Cena en el Mercado 20 de Noviembre","descripcion":"Probar tlayudas y mole.","dia":"2026-12-22","hora":"20:00","costoEstimado":400.00,"autor":{"id":2,"nombre":"Luis Pérez"},"totalVotos":1,"votadaPorMi":false}],"actividadMasVotada":{"id":2,"titulo":"Cena en el Mercado 20 de Noviembre","totalVotos":1}},{"fecha":"2026-12-23","actividades":[],"actividadMasVotada":null},{"fecha":"2026-12-24","actividades":[],"actividadMasVotada":null},{"fecha":"2026-12-25","actividades":[],"actividadMasVotada":null},{"fecha":"2026-12-26","actividades":[],"actividadMasVotada":null},{"fecha":"2026-12-27","actividades":[],"actividadMasVotada":null}]} [200]

$ curl -s -b /tmp/luis.txt localhost:8080/api/viajes/1 | python3 /tmp/dias.py
2026-12-20 null
2026-12-21 {"id": 1, "titulo": "Visita a Hierve el Agua", "totalVotos": 2}
2026-12-22 {"id": 2, "titulo": "Cena en el Mercado 20 de Noviembre", "totalVotos": 1}
2026-12-23 null
2026-12-24 null
2026-12-25 null
2026-12-26 null
2026-12-27 null

# Preparación: el 22-dic ya tiene la Cena (400, 1 voto). Luis propone otra más barata y vota por ella.
$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' -H 'Content-Type: application/json' -d '{"titulo":"Mezcalería Los Amantes","dia":"2026-12-22","hora":"18:00","costoEstimado":150}' localhost:8080/api/viajes/1/actividades
{"id":3,"viajeId":1,"titulo":"Mezcalería Los Amantes","descripcion":null,"dia":"2026-12-22","hora":"18:00","costoEstimado":150.00,"autor":{"id":2,"nombre":"Luis Pérez"},"totalVotos":0,"votadaPorMi":false} [201]

$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' -X POST localhost:8080/api/actividades/3/votos
{"actividadId":3,"totalVotos":1,"votadaPorMi":true} [201]

# Preparación: el 23-dic Ana propone dos actividades del mismo costo y Luis vota por las dos.
$ curl -s -b /tmp/ana.txt -w ' [%{http_code}]\n' -H 'Content-Type: application/json' -d '{"titulo":"Zona arqueológica de Monte Albán","dia":"2026-12-23","hora":"09:00","costoEstimado":200}' localhost:8080/api/viajes/1/actividades
{"id":4,"viajeId":1,"titulo":"Zona arqueológica de Monte Albán","descripcion":null,"dia":"2026-12-23","hora":"09:00","costoEstimado":200.00,"autor":{"id":1,"nombre":"Ana López"},"totalVotos":0,"votadaPorMi":false} [201]

$ curl -s -b /tmp/ana.txt -w ' [%{http_code}]\n' -H 'Content-Type: application/json' -d '{"titulo":"Zona arqueológica de Mitla","dia":"2026-12-23","hora":"13:00","costoEstimado":200}' localhost:8080/api/viajes/1/actividades
{"id":5,"viajeId":1,"titulo":"Zona arqueológica de Mitla","descripcion":null,"dia":"2026-12-23","hora":"13:00","costoEstimado":200.00,"autor":{"id":1,"nombre":"Ana López"},"totalVotos":0,"votadaPorMi":false} [201]

$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' -X POST localhost:8080/api/actividades/4/votos
{"actividadId":4,"totalVotos":1,"votadaPorMi":true} [201]

$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' -X POST localhost:8080/api/actividades/5/votos
{"actividadId":5,"totalVotos":1,"votadaPorMi":true} [201]

# Preparación: el 24-dic tiene una actividad sin votos.
$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' -H 'Content-Type: application/json' -d '{"titulo":"Tianguis de Tlacolula","dia":"2026-12-24","hora":"10:00","costoEstimado":0}' localhost:8080/api/viajes/1/actividades
{"id":6,"viajeId":1,"titulo":"Tianguis de Tlacolula","descripcion":null,"dia":"2026-12-24","hora":"10:00","costoEstimado":0.00,"autor":{"id":2,"nombre":"Luis Pérez"},"totalVotos":0,"votadaPorMi":false} [201]
```

Estado en MySQL de las actividades del viaje después de la preparación:
```text
$ mysql -u bitacora_app -p bitacora_viajes -e "SELECT id, titulo, dia, costo_estimado, fecha_creacion FROM actividad WHERE viaje_id = 1 ORDER BY id;"
id	titulo	dia	costo_estimado	fecha_creacion
1	Visita a Hierve el Agua	2026-12-21	350.00	2026-11-13 23:29:15
2	Cena en el Mercado 20 de Noviembre	2026-12-22	400.00	2026-11-13 23:29:15
3	Mezcalería Los Amantes	2026-12-22	150.00	2026-11-16 23:21:11
4	Zona arqueológica de Monte Albán	2026-12-23	200.00	2026-11-16 23:21:12
5	Zona arqueológica de Mitla	2026-12-23	200.00	2026-11-16 23:21:12
6	Tianguis de Tlacolula	2026-12-24	0.00	2026-11-16 23:21:12
```
Monte Albán y Mitla quedaron con la misma `fecha_creacion` porque se crearon en el mismo segundo y la columna es `DATETIME` (sin fracciones de segundo): empate total real.

```text
# 2-4. Empate en votos → menor costo (22-dic), empate total (23-dic) y día con actividad pero sin votos (24-dic)
$ curl -s -b /tmp/luis.txt localhost:8080/api/viajes/1 | python3 /tmp/dias.py
2026-12-20 null
2026-12-21 {"id": 1, "titulo": "Visita a Hierve el Agua", "totalVotos": 2}
2026-12-22 {"id": 3, "titulo": "Mezcalería Los Amantes", "totalVotos": 1}
2026-12-23 null
2026-12-24 null
2026-12-25 null
2026-12-26 null
2026-12-27 null
```
- **22-dic:** Cena y Mezcalería tienen 1 voto cada una; gana la Mezcalería porque cuesta 150 contra 400.
- **23-dic:** Monte Albán y Mitla empatan en votos (1), costo (200) y fecha de creación → `null`.
- **24-dic:** el Tianguis no tiene votos → `null`.

Para probar el desempate por fecha se movió con SQL la creación de Monte Albán a un momento posterior. Mitla va segunda en la lista del día (13:00), así se comprueba que gana por ser la creada primero y no por el orden:
```text
$ mysql -u bitacora_app -p bitacora_viajes -e "UPDATE actividad SET fecha_creacion = '2026-11-16 23:30:00' WHERE id = 4;"

# 5. Empate en votos y costo: gana la creada primero (Mitla)
$ curl -s -b /tmp/luis.txt localhost:8080/api/viajes/1 | python3 /tmp/dias.py
2026-12-20 null
2026-12-21 {"id": 1, "titulo": "Visita a Hierve el Agua", "totalVotos": 2}
2026-12-22 {"id": 3, "titulo": "Mezcalería Los Amantes", "totalVotos": 1}
2026-12-23 {"id": 5, "titulo": "Zona arqueológica de Mitla", "totalVotos": 1}
2026-12-24 null
2026-12-25 null
2026-12-26 null
2026-12-27 null

# 6. El detalle conserva sus validaciones: viaje inexistente → 404
$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/999
{"codigo":404,"mensaje":"El viaje no existe"} [404]

# 7. Usuario que no participa → 403
$ curl -s -b /tmp/pepe.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/1
{"codigo":403,"mensaje":"No participas en este viaje"} [403]
```

**Limpieza:** al terminar se eliminaron las actividades de prueba (sus votos se borran con ellas) y el detalle volvió al estado original:
```text
$ curl -s -b /tmp/luis.txt -o /dev/null -w '[%{http_code}]\n' -X DELETE localhost:8080/api/actividades/3
[204]

$ curl -s -b /tmp/ana.txt -o /dev/null -w '[%{http_code}]\n' -X DELETE localhost:8080/api/actividades/4
[204]

$ curl -s -b /tmp/ana.txt -o /dev/null -w '[%{http_code}]\n' -X DELETE localhost:8080/api/actividades/5
[204]

$ curl -s -b /tmp/luis.txt -o /dev/null -w '[%{http_code}]\n' -X DELETE localhost:8080/api/actividades/6
[204]

$ curl -s -b /tmp/luis.txt localhost:8080/api/viajes/1 | python3 /tmp/dias.py
2026-12-20 null
2026-12-21 {"id": 1, "titulo": "Visita a Hierve el Agua", "totalVotos": 2}
2026-12-22 {"id": 2, "titulo": "Cena en el Mercado 20 de Noviembre", "totalVotos": 1}
2026-12-23 null
2026-12-24 null
2026-12-25 null
2026-12-26 null
2026-12-27 null
```

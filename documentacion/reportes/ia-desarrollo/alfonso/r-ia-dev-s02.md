# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 13-11-2026
**Número de reporte:** 02 (Alfonso Mendoza Vásquez - HU-16 Consultar presupuesto)

---

# 1. Breve descripción de uso

Implementación de `GET /api/viajes/{id}/presupuesto`, que devuelve el presupuesto total, el total gastado, lo restante, el porcentaje usado, el número de participantes y el gasto por persona. El cálculo vive en `PresupuestoService`, usa `BigDecimal` a 2 decimales y nunca divide entre cero.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Sonnet 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `7c0b87df5a550fd1cfc636e950d6909c94e423fa` (HU-14: expone POST y DELETE /api/actividades/{id}/votos) |
| **Código posterior** | Commit `2ed0a8d728850ba900742beb7af8c6b7d9a9ed9f` (HU-16: agrega pruebas JUnit (sin gastos, dentro, excedido)) |
| **Diferencias entre versiones** | [Ver diferencias](#diferencias-entre-versiones) |   
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Aprendí a usar `BigDecimal` en lugar de `double` para el dinero, porque `double` genera errores de redondeo. También a cuidar el caso sin gastos para no dividir entre cero, y a dejar el cálculo en el servicio para poder probarlo con JUnit sin levantar el servidor. Las 3 pruebas (sin gastos, dentro y excedido) me sirvieron para comprobar las fórmulas. |

---

### Prompt original
> Ya refine todo e hice el push, ahora necesito pasemos a HU-16, lo mismo, HU-16 Consultar presupuesto. Como participante de un viaje, quiero consultar el estado del presupuesto, para saber cuánto se ha gastado, cuánto falta y cuánto toca por persona
>Alcance: construyo el servicio del cálculo; alimenta la barra de progreso de HU-32.

>GET /api/viajes/{id}/presupuesto devuelve {presupuestoTotal, totalGastado, restante, porcentajeUsado, numParticipantes, gastoPorPersona, monedaBase}.
>El cálculo vive en PresupuestoService, no en el controlador, con BigDecimal a 2 decimales.
>Sin gastos: totalGastado 0 y porcentaje 0, nunca división entre cero.
>>Solo participantes (403); viaje inexistente (404).
>Pruebas JUnit con 3 casos: sin gastos, dentro del presupuesto, excedido.

"Lo mismo" se refiere a las indicaciones dadas antes en la misma conversación para HU-14 (hacer la historia completa con código limpio, sencillo y entendible). Los criterios de aceptación de HU-16 se tomaron del documento de historias de usuario del equipo y del catálogo `documentacion/api.md`.

### Respuesta de IA
Se generaron cuatro archivos:
- `PresupuestoResponse`: record con los 7 campos del catálogo (`presupuestoTotal`, `totalGastado`, `restante`, `porcentajeUsado`, `numParticipantes`, `gastoPorPersona`, `monedaBase`).
- `PresupuestoService`: el método `obtenerPresupuesto` trae los datos de la base de datos y reutiliza `buscarParticipacion` de `ViajeDetalleService` para responder 404 (viaje inexistente) y 403 (no participa); el método `calcular` hace las cuentas (restante = total − gastado, porcentaje = gastado × 100 / total, por persona = gastado / participantes) con `BigDecimal` y `RoundingMode.HALF_UP`.
- `PresupuestoController`: expone `GET /api/viajes/{id}/presupuesto` y toma al usuario de la sesión; no hace cálculos.
- `PresupuestoServiceTest`: 3 pruebas JUnit del método `calcular`, sin base de datos: sin gastos, dentro del presupuesto y excedido.

### Diferencias entre versiones
```text
15	1	backend/src/main/java/com/bitacora/viajes/dto/PresupuestoResponse.java
86	1	backend/src/main/java/com/bitacora/viajes/service/PresupuestoService.java
42	1	backend/src/main/java/com/bitacora/viajes/controller/PresupuestoController.java
49	1	backend/src/test/java/com/bitacora/viajes/service/PresupuestoServiceTest.java
```

---

### Pruebas

**Pruebas unitarias (JUnit)**, ejecutadas en WSL:
```text
$ cd backend && ./mvnw test -Dtest=PresupuestoServiceTest
-------------------------------------------------------------------------------
Test set: com.bitacora.viajes.service.PresupuestoServiceTest
-------------------------------------------------------------------------------
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.404 s -- in com.bitacora.viajes.service.PresupuestoServiceTest
```

**Pruebas con cURL** contra el servidor local, con los datos de prueba del `DataLoader` (viaje 1: presupuesto de 15000 pesos, 2 participantes y 2 gastos):
```text
$ curl -s -c /tmp/luis.txt -H "Content-Type: application/json" -d '{"correo":"luis@example.com","contrasena":"Password123"}' localhost:8080/api/auth/login

# 1. Presupuesto del viaje → 200
$ curl -s -b /tmp/luis.txt -w " [%{http_code}]\n" localhost:8080/api/viajes/1/presupuesto
{"presupuestoTotal":15000.00,"totalGastado":5025.00,"restante":9975.00,"porcentajeUsado":33.50,"numParticipantes":2,"gastoPorPersona":2512.50,"monedaBase":"MXN"} [200]

# 2. Viaje inexistente → 404
$ curl -s -b /tmp/luis.txt -w " [%{http_code}]\n" localhost:8080/api/viajes/999/presupuesto
{"campo":"viaje","mensaje":"El viaje no existe"} [404]

# 3. Usuario que no participa (pepe@example.com) → 403
$ curl -s -b /tmp/pepe.txt -w " [%{http_code}]\n" localhost:8080/api/viajes/1/presupuesto
{"campo":"viaje","mensaje":"No participas en este viaje"} [403]

# 4. Sin sesión → 401
$ curl -s -o /dev/null -w "[%{http_code}]\n" localhost:8080/api/viajes/1/presupuesto
[401]

# 5. Recálculo: después de registrar un gasto de 500 con POST /api/gastos
$ curl -s -b /tmp/luis.txt -w " [%{http_code}]\n" localhost:8080/api/viajes/1/presupuesto
{"presupuestoTotal":15000.00,"totalGastado":5525.00,"restante":9475.00,"porcentajeUsado":36.83,"numParticipantes":2,"gastoPorPersona":2762.50,"monedaBase":"MXN"} [200]
```

**Comprobación en MySQL** del caso 5 (cálculo manual):
```text
$ mysql -u bitacora_app -p bitacora_viajes -e "SELECT v.presupuesto_total, SUM(g.monto) gastado, (SELECT COUNT(*) FROM participante_viaje WHERE viaje_id=1) personas FROM viaje v JOIN gasto g ON g.viaje_id=v.id WHERE v.id=1 GROUP BY v.id;"
presupuesto_total	gastado	personas
15000.00	5525.00	2
```
5525.00 / 15000.00 × 100 = 36.83 % y 5525.00 / 2 = 2762.50, igual que la respuesta del endpoint. Al terminar, el gasto de prueba se eliminó con `DELETE /api/gastos/3` (204) y el presupuesto volvió a 5025.00.

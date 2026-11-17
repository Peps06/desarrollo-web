# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 16-11-2026
**Número de reporte:** 03 (Alfonso Mendoza Vásquez - HU-21 Balance de deudas del grupo)

---

# 1. Breve descripción de uso

Implementación de `GET /api/viajes/{id}/balance`, que devuelve la cuota por persona, el total gastado, el saldo de cada participante (lo que pagó menos la cuota) y la lista de transferencias necesarias para que todos queden a mano. Las transferencias se generan emparejando al que más debe con al que más le deben hasta dejar todos los saldos en 0. Los montos usan `BigDecimal` a 2 decimales con `RoundingMode.HALF_UP`, y si por el redondeo de la cuota la suma de los saldos no da 0, la diferencia se ajusta para que cuadre.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude Code (Anthropic), extensión de Visual Studio Code |
| **Modelo/temperatura** | Claude Opus 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `ce36470f5218fd302072d756b27a77750836bdef` (Merge branch 'main' of https://github.com/Peps06/desarrollo-web) |
| **Código posterior** | Commit `7b106317903d6e29a20e93d35448e7520f315114` (HU-21: balance de deudas del grupo con transferencias y pruebas JUnit) |
| **Diferencias entre versiones** | [Ver diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Aprendí que repartir gastos no es solo dividir: hay que calcular el saldo de cada quien y luego decidir quién le paga a quién. El algoritmo de emparejar al que más debe con al que más le deben es sencillo y siempre termina, porque en cada vuelta al menos una persona queda en 0. También vi que al dividir con `BigDecimal` puede sobrar un centavo (100 / 3 = 33.33 × 3 = 99.99) y que hay que ajustarlo para que la suma de saldos dé exactamente 0. Separar el cálculo en `calcular()` me permitió probarlo con JUnit sin base de datos, y sumar los pagos con `GROUP BY` en la consulta evita cargar todos los gastos. |

---

### Prompt original
> Bitácora de viajes (Spring Boot 3.2.5, Java 17, MySQL). Ahora vamos a HU-21: Balance de deudas del grupo. Los archivos ya existen pero están vacíos (solo tienen un comentario): BalanceService.java, BalanceController.java, BalanceServiceTest.java y los DTO BalanceResponse.java, SaldoResponse.java y TransferenciaResponse.java.
>
> Requisitos:
> - GET /api/viajes/{id}/balance devuelve {cuotaPorPersona, totalGastado, saldos:[{usuarioId, nombre, pagado, saldo}], transferencias:[{deudorId, deudorNombre, acreedorId, acreedorNombre, monto}]}.
> - cuota = totalGastado / número de participantes. saldo de cada uno = lo que pagó - cuota (positivo: le deben; negativo: debe).
> - Las transferencias se generan emparejando al que más debe con al que más le deben, hasta dejar todos los saldos en 0.
> - BigDecimal con 2 decimales y RoundingMode.HALF_UP. La suma de los saldos debe dar 0; si sobra un centavo por redondeo, ajústalo para que cuadre.
> - Sin gastos: saldos en 0 y transferencias vacías. Con un solo participante: sin transferencias.
> - Solo participantes: 403. Viaje inexistente: 404. Reutiliza viajeDetalleService.buscarParticipacion(viajeId, correoUsuario), igual que en PresupuestoService.
> - Usa participanteViajeRepository.buscarPorViaje(viajeId) para los participantes. Para lo que pagó cada uno, agrega a GastoRepository una consulta que sume monto agrupado por usuario.id en el viaje, sin cargar todos los gastos.
> - Igual que en PresupuestoService, separa un método público calcular(...) que no use la base de datos, para probarlo con JUnit.
> - DTO como record. El usuario se toma de la sesión (Principal), como en PresupuestoController.
>
> Pruebas JUnit en BalanceServiceTest:
> 1. 3 personas: A pagó 600, B y C pagaron 0. Cuota 200; B le paga 200 a A y C le paga 200 a A.
> 2. Sin gastos: todo en 0 y sin transferencias.
> 3. Un solo participante: sin transferencias.
> 4. Un caso con decimales (por ejemplo 100 entre 3) donde la suma de saldos dé 0.00.
>
> Crea el código completo de cada archivo, después los comandos cURL para probar (éxito, 403 y 404) y al final explícame en palabras simples cómo funciona el algoritmo de las transferencias, porque lo tengo que explicar al profesor.

Instrucción general de estilo dada en conversaciones anteriores:
> "todo el codigo que consideres añadir o modificar debe ser: Limpio, sencillo, y entendible. Para que yo y cualquiera lo entienda completamente"

### Respuesta de IA
Se generaron seis archivos y se modificó uno:
- `SaldoResponse`, `TransferenciaResponse` y `BalanceResponse`: records con los campos del catálogo `documentacion/api.md` (endpoint 24).
- `GastoRepository` (modificado): nueva consulta `sumarMontoPorUsuario(viajeId)` con `GROUP BY g.usuario.id`; devuelve una fila `[usuarioId, total]` por cada persona que pagó, sin cargar los gastos.
- `BalanceService`: el método `obtenerBalance` reutiliza `buscarParticipacion` de `ViajeDetalleService` (404 viaje inexistente o cancelado, 403 no participa), arma un mapa `usuarioId → nombre` con `buscarPorViaje` y otro `usuarioId → pagado` con la consulta nueva. El método público `calcular` no usa la base de datos y hace 6 pasos: total, cuota (`HALF_UP`), saldo de cada uno, ajuste de redondeo (la diferencia se le resta al saldo más alto), lista de saldos y transferencias (bucle que empareja el saldo más negativo con el más positivo y transfiere la menor de las dos cantidades).
- `BalanceController`: expone `GET /api/viajes/{viajeId}/balance` y toma al usuario de la sesión; no hace cálculos.
- `BalanceServiceTest`: 4 pruebas JUnit del método `calcular`, sin base de datos.

Además se dieron los comandos cURL de prueba y una explicación paso a paso del algoritmo de transferencias con un ejemplo de 4 personas. Decisión señalada por la IA: el total se calcula con lo que pagaron los participantes actuales, para que la suma de saldos siempre dé 0 aunque alguien haya salido del viaje.

### Diferencias entre versiones
```text
35	1	backend/src/main/java/com/bitacora/viajes/controller/BalanceController.java
13	1	backend/src/main/java/com/bitacora/viajes/dto/BalanceResponse.java
12	1	backend/src/main/java/com/bitacora/viajes/dto/SaldoResponse.java
12	1	backend/src/main/java/com/bitacora/viajes/dto/TransferenciaResponse.java
8	0	backend/src/main/java/com/bitacora/viajes/repository/GastoRepository.java
197	1	backend/src/main/java/com/bitacora/viajes/service/BalanceService.java
120	1	backend/src/test/java/com/bitacora/viajes/service/BalanceServiceTest.java
```

---

### Pruebas

**Pruebas unitarias (JUnit)**, ejecutadas en WSL:
```text
$ cd backend && ./mvnw test -Dtest=BalanceServiceTest
-------------------------------------------------------------------------------
Test set: com.bitacora.viajes.service.BalanceServiceTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.063 s -- in com.bitacora.viajes.service.BalanceServiceTest
```

| Prueba | Qué comprueba |
| :--- | :--- |
| `unoPagoTodoYLosOtrosDosLePagan` | Ana pagó 600, Beto y Cris 0: cuota 200, saldos +400 / −200 / −200, Beto → Ana 200 y Cris → Ana 200 |
| `sinGastosTodoEnCero` | Sin pagos: total, cuota, pagado y saldos en 0.00 y sin transferencias |
| `unSoloParticipanteNoTieneTransferencias` | Una persona que pagó 1,500: saldo 0.00 y sin transferencias |
| `conDecimalesLaSumaDeSaldosDaCero` | 100 entre 3: cuota 33.33, el centavo sobrante se ajusta (66.66 / −33.33 / −33.33) y la suma da 0.00 |

**Pruebas con cURL**, ejecutadas desde Claude Code en WSL contra el servidor local (`./mvnw spring-boot:run`), con los datos de prueba del `DataLoader` (viaje 1: Ana pagó 4,500 y Luis 525; `pepe@example.com` no participa en el viaje):
```text
$ curl -s -c /tmp/luis.txt -H 'Content-Type: application/json' -d '{"correo":"luis@example.com","contrasena":"Password123"}' localhost:8080/api/auth/login
{"id":2,"nombre":"Luis Pérez","correo":"luis@example.com"}
$ curl -s -c /tmp/ana.txt -H 'Content-Type: application/json' -d '{"correo":"ana@example.com","contrasena":"Password123"}' localhost:8080/api/auth/login
{"id":1,"nombre":"Ana López","correo":"ana@example.com"}
$ curl -s -c /tmp/pepe.txt -H 'Content-Type: application/json' -d '{"correo":"pepe@example.com","contrasena":"Password123"}' localhost:8080/api/auth/login
{"id":3,"nombre":"Pepe Prueba","correo":"pepe@example.com"}

# 1. Balance del viaje 1 → 200
$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/1/balance
{"cuotaPorPersona":2512.50,"totalGastado":5025.00,"saldos":[{"usuarioId":1,"nombre":"Ana López","pagado":4500.00,"saldo":1987.50},{"usuarioId":2,"nombre":"Luis Pérez","pagado":525.00,"saldo":-1987.50}],"transferencias":[{"deudorId":2,"deudorNombre":"Luis Pérez","acreedorId":1,"acreedorNombre":"Ana López","monto":1987.50}]} [200]

# 2. Viaje inexistente → 404
$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/999/balance
{"codigo":404,"mensaje":"El viaje no existe"} [404]

# 3. Usuario que no participa (pepe@example.com) → 403
$ curl -s -b /tmp/pepe.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/1/balance
{"codigo":403,"mensaje":"No participas en este viaje"} [403]

# 4. Sin sesión → 401
$ curl -s -o /dev/null -w '[%{http_code}]\n' localhost:8080/api/viajes/1/balance
[401]

# 5. Caso con decimales: Ana agrega a Pepe al viaje y Pepe registra un gasto de 100
$ curl -s -b /tmp/ana.txt -w ' [%{http_code}]\n' -H 'Content-Type: application/json' -d '{"correo":"pepe@example.com"}' localhost:8080/api/viajes/1/participantes
{"usuarioId":3,"nombre":"Pepe Prueba","correo":"pepe@example.com","rol":"participante"} [201]

$ curl -s -b /tmp/pepe.txt -w ' [%{http_code}]\n' -H 'Content-Type: application/json' -d '{"viajeId":1,"concepto":"Agua y botanas","categoria":"COMIDA","monto":100,"monedaOriginal":"MXN","montoOriginal":100,"tipoCambio":1}' localhost:8080/api/gastos
{"gasto":{"id":4,"viajeId":1,"concepto":"Agua y botanas","categoria":"COMIDA","monto":100,"fecha":"2026-11-16T23:20:14.267138729","usuarioId":3},"totalViaje":5125.00} [201]

$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/1/balance
{"cuotaPorPersona":1708.33,"totalGastado":5125.00,"saldos":[{"usuarioId":1,"nombre":"Ana López","pagado":4500.00,"saldo":2791.66},{"usuarioId":2,"nombre":"Luis Pérez","pagado":525.00,"saldo":-1183.33},{"usuarioId":3,"nombre":"Pepe Prueba","pagado":100.00,"saldo":-1608.33}],"transferencias":[{"deudorId":3,"deudorNombre":"Pepe Prueba","acreedorId":1,"acreedorNombre":"Ana López","monto":1608.33},{"deudorId":2,"deudorNombre":"Luis Pérez","acreedorId":1,"acreedorNombre":"Ana López","monto":1183.33}]} [200]
```

**Comprobación en MySQL** del caso 5 (cálculo manual):
```text
$ mysql -u bitacora_app -p bitacora_viajes -e "SELECT usuario_id, SUM(monto) pagado FROM gasto WHERE viaje_id = 1 GROUP BY usuario_id; SELECT COUNT(*) personas FROM participante_viaje WHERE viaje_id = 1;"
usuario_id	pagado
1	4500.00
2	525.00
3	100.00
personas
3
```
- Total: 4500 + 525 + 100 = 5125.00; cuota: 5125 / 3 = 1708.333… → **1708.33** (`HALF_UP`).
- Saldos sin ajuste: Ana 4500 − 1708.33 = 2791.67, Luis 525 − 1708.33 = −1183.33, Pepe 100 − 1708.33 = −1608.33. Suman **+0.01** (porque 1708.33 × 3 = 5124.99).
- El centavo sobrante se le resta al saldo más alto: Ana queda en **2791.66** y la suma da 0.00.
- Transferencias: primero paga el que más debe (Pepe, 1608.33) y después Luis (1183.33). 1608.33 + 1183.33 = 2791.66, justo lo que le deben a Ana.

**Limpieza:** al terminar se eliminaron el gasto y el participante de prueba, y el balance volvió al estado original:
```text
$ curl -s -b /tmp/pepe.txt -o /dev/null -w '[%{http_code}]\n' -X DELETE localhost:8080/api/gastos/4
[204]

$ curl -s -b /tmp/ana.txt -o /dev/null -w '[%{http_code}]\n' -X DELETE localhost:8080/api/viajes/1/participantes/3
[204]

$ curl -s -b /tmp/luis.txt -w ' [%{http_code}]\n' localhost:8080/api/viajes/1/balance
{"cuotaPorPersona":2512.50,"totalGastado":5025.00,"saldos":[{"usuarioId":1,"nombre":"Ana López","pagado":4500.00,"saldo":1987.50},{"usuarioId":2,"nombre":"Luis Pérez","pagado":525.00,"saldo":-1987.50}],"transferencias":[{"deudorId":2,"deudorNombre":"Luis Pérez","acreedorId":1,"acreedorNombre":"Ana López","monto":1987.50}]} [200]
```

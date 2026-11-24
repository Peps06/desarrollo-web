# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 19-11-2026
**Número de reporte:** 07 (Dana Carmona Santamaria)

---
> # Login y registro en el frontend
## 1. Breve descripción de uso

Uso de Claude para hacer las páginas de inicio de sesión y de registro con Thymeleaf y el JavaScript que revisa los datos antes de enviarlos . Los errores se muestran debajo de cada campo o como mensaje general, sin recargar la página.

---

## 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `9a9bfe0c4d2d818fe03b96dc9ef564452623fb3b` |
| **Código posterior** | Commit `702fff77765d6726d7748f55771c5997bd00d4b4` |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#pruebas) |
| **Código de uso** | Generación y modificación |
| **Reflexión del estudiante** | Durante esta sesión realice el html correspondiente de login.html y resgistro, también ocupe la ia para que me ayudara a realizar las validaciones nesearias para completar los campos, por lo que el enfoque principal fueron los errores que se muestran. |

---

### Prompt original
> "Quiero las páginas de login y registro. Login y registro con validación al instante: correo con formato válido, contraseña de mínimo 8 caracteres y nombre no vacío. El error aparece junto al campo. Enviar con fetch(); los errores del servidor (correo ocupado, credenciales incorrectas) se muestran sin recargar y sin borrar lo escrito. Login correcto lleva a la lista de viajes.

### Respuesta de IA
Claude revisó el código que ya existía en el repo. Escribió de nuevo `login.html`, porque la versión de prueba tenía credenciales escritas y clases que no existen en el CSS. También hizo `registro.html`, `validacion.js` (las reglas de cada campo) y `auth.js` (el envío con `fetch()` y el manejo de la respuesta). Como el equipo ya usa Thymeleaf, se descartó el primer enfoque, que era usar HTML aparte con CORS.

### Diferencias entre versiones
```
git diff --numstat 9a9bfe0c4d2d818fe03b96dc9ef564452623fb3b 702fff77765d6726d7748f55771c5997bd00d4b4
0       0       backend/src/main/resources/static/js/.gitkeep
135     0       backend/src/main/resources/static/js/auth.js
84      0       backend/src/main/resources/static/js/validacion.js
2       0       backend/src/main/resources/templates/fragments/base.html
23      38      backend/src/main/resources/templates/login.html
40      1       backend/src/main/resources/templates/registro.html
135     1       frontend/js/auth.js
84      1       frontend/js/validacion.js
```
---

### Pruebas

```
# Registro nuevo: 201
dana@Dana:~/desarrollo-web$ curl -i -X POST http://localhost:8081/api/auth/r
egistro -H "Content-Type: application/json" -d '{"nombre":"Prueba","correo":
"prueba2@ejemplo.com","contrasena":"Password123"}'
HTTP/1.1 201
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 19 Nov 2026 22:40:24 GMT

{"id":7,"nombre":"Prueba","correo":"prueba2@ejemplo.com"}

# Login incorrecto:  401
curl -i -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"correo":"prueba2@ejemplo.com"
,"contrasena":"otra"}'
curl: (7) Failed to connect to localhost port 8080 after 0 ms: Could not connect to server
dana@Dana:~/desarrollo-web$ curl -i -X POST http://localhost:8081/api/auth/l
ogin -H "Content-Type: application/json" -d '{"correo":"prueba2@ejemplo.com"
,"contrasena":"otra"}'
HTTP/1.1 401
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 19 Nov 2026 22:42:28 GMT

{"codigo":401,"mensaje":"Correo o contraseña incorrectos"}
```
---

## Observaciones adicionales
No fue algo dificil de hacer ya que utilice elementos básicos y que ya existian del proyecto. La razón principal por la que use ia fue para comprender las validaciones necesarias y la forma en que se muestran los erroes, pero también tome ayudame para la vista de los html.

---

> # Balance del grupo
## 1. Breve descripción de uso

Uso de Claude para crear la sección «Balance del grupo» en el detalle del viaje: un archivo JavaScript que pide el balance al backend y lo muestra en tarjetas por participante, con la lista de transferencias para saldar cuentas, sin recargar la página.

## 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original-balance-del-grupo) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia-balance-del-grupo) |
| **Código anterior** | `70a216419de1346208249041a1598ea7b35d4417` |
| **Código posterior** | `5f1c4393f450922f9619696ecadcca6c0ace6968` |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones-balance-del-grupo) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#pruebas-balance-del-grupo) |
| **Código de uso** | Generación y modificación |
| **Reflexión del estudiante** | Fue la primera vez que tuve que leer un endpoint y varios archivos del proyecto antes de escribir código. Claude me ayudó a entender qué datos mandaba el backend y cómo pintarlos sin recargar la página. |

---

### Prompt original
> "Como participante, quiero ver quién le debe a quién en tarjetas claras, para saldar cuentas al final del viaje. Sección «Balance del grupo» con una tarjeta por integrante: verde si le deben, rojo si debe, neutra si está en 0. Lista de transferencias con frases, resaltando al usuario en sesión. Se actualiza sin recargar al registrar un gasto."

### Respuesta de IA
Claude leyó el endpoint del balance, el archivo de presupuesto como patrón y las clases CSS que ya existían. Propuso una tarjeta por participante, con verde si le deben, rojo si debe y neutra si está en cero, y una lista de frases con las transferencias. Como el archivo de la página no guardaba el usuario en sesión, Claude propuso pedirlo con `GET /api/auth/sesion`. Para no mostrar datos viejos, también propuso un contador de peticiones.

### Diferencias entre versiones
```
git diff --numstat 70a216419de1346208249041a1598ea7b35d4417 5f1c4393f450922f9619696ecadcca6c0ace6968
5       0       .gitignore
0       5       backend/cookies_part.txt
9       0       backend/pom.xml
0       203     backend/src/main/resources/static/css/base.css
0       525     backend/src/main/resources/static/css/componentes.css
0       79      backend/src/main/resources/static/css/presupuesto.css
0       87      backend/src/main/resources/static/css/responsive.css
0       65      backend/src/main/resources/static/css/variables.css
0       135     backend/src/main/resources/static/js/auth.js
0       216     backend/src/main/resources/static/js/checklist.js
0       122     backend/src/main/resources/static/js/mensajes.js
0       84      backend/src/main/resources/static/js/validacion.js
0       942     backend/src/main/resources/static/js/viaje-detalle.js
0       141     backend/src/main/resources/static/js/viaje-nuevo.js
0       145     backend/src/main/resources/static/js/viajes-lista.js
1       0       backend/src/main/resources/templates/viajes/detalle.html
228     1       frontend/js/balance.js
```

### Pruebas 

```
// Desde la consola del navegador, en /viajes/{id} con sesión iniciada
const viajeId = document.getElementById("paginaDetalle").dataset.viajeId;

// El endpoint responde 200
await fetch(`/api/viajes/${viajeId}/balance`, {credentials: "same-origin"}).then(r => r.status)
resultado: 200

// Los saldos suman 0.00
const d = await fetch(`/api/viajes/${viajeId}/balance`, {credentials: "same-origin"}).then(r => r.json());
d.saldos.reduce((s, p) => s + p.saldo, 0).toFixed(2)
resultado: "0.00"

// Una tarjeta por participante
document.querySelectorAll("#listaSaldos > .tarjeta").length
resultado: 0

// Se actualiza sin recargar (debe aparecer una petición nueva en Network)
document.dispatchEvent(new CustomEvent("gasto-registrado"))
resultado: true
```

---

### Observaciones
Tuve que realizar varias pruebas para comprobar el funcionamiento.

---

> # Conversor de divisas
## 1. Breve descripción de uso

Uso de Claude para agregar el conversor de divisas al formulario de gastos: un selector de moneda (MXN, USD, EUR) y un archivo JavaScript que consulta el tipo de cambio a Frankfurter, muestra el equivalente en la moneda del viaje antes de enviar y guarda el monto ya convertido con su moneda original y su tipo de cambio, con aviso si no hay internet.

## 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original-conversor-de-divisas) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia-conversor-de-divisas) |
| **Código anterior** | Commit `f4c4197~1` (`divisas.js` solo tenía un comentario) |
| **Código posterior** | Commit `f4c4197dfdac9078351e8c3636b06a2df8ad9e0d` |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones-conversor-de-divisas) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#pruebas-conversor-de-divisas) |
| **Código de uso** | Generación y modificación |
| **Reflexión del estudiante** | Gracias a la ayuda de la ia, conocí a Frankfurter, que es una Api que me ayuda a realizar la conversion de monedas. También tuve que cuidar que la aplicación siguiera funcionando sin internet, y que el gasto no cambiara después de guardarlo. |

---

### Prompt original
> "Como usuario que realiza un viaje internacional, quiero registrar gastos en una moneda extranjera y verlos convertidos a la moneda del viaje, para llevar un presupuesto preciso sin calcular a mano. Selector de moneda (MXN, USD, EUR). Al escribir el monto, consultar un servicio externo gratuito y mostrar el equivalente antes de enviar. Si el servicio falla, permitir capturar el monto en la moneda del viaje."

### Respuesta de IA 
Claude propuso pedir el tipo de cambio a Frankfurter con `fetch()`, guardarlo para no repetir la consulta y convertir el monto antes de enviarlo. Si el servicio falla, el formulario debe seguir funcionando con la moneda del viaje. Claude también señaló que el backend no convierte: el frontend debe mandar el monto ya convertido, junto con la moneda original y el tipo de cambio.

### Diferencias entre versiones 
```
git diff --numstat 5f1c4393f450922f9619696ecadcca6c0ace6968 f4c4197dfdac9078351e8c3636b06a2df8ad9e0d
1       1       backend/src/main/resources/templates/viajes/detalle.html
86      1       frontend/js/divisas.js
103     6       frontend/js/gastos.js
```

### Pruebas

```
// Desde la consola del navegador
const viajeId = document.getElementById("paginaDetalle").dataset.viajeId;

// Conversión
convertirAMonedaBase(20, 18)
resultado: 360

// Formato
formatearMontoDivisa(360, "MXN")
resultado: "$360.00 MXN"

// Con internet
await obtenerTipoCambio("USD", "MXN")
resultado: 19.0123

// Sin internet
await obtenerTipoCambio("GBP", "MXN")
resultado: error "Failed to fetch"

// Selector de moneda
const s = document.createElement("select");
llenarSelectorMoneda(s, "EUR");
[...s.options].map(o => o.value).join(",") + " / " + s.value
resultado: "EUR,MXN,USD / EUR"

// Gasto guardado con su moneda original
const gastos = await fetch(`/api/viajes/${viajeId}/gastos`, {credentials: "same-origin"}).then(r => r.json());
gastos[0].monedaOriginal + " " + gastos[0].montoOriginal + " " + gastos[0].tipoCambio
resultado: "USD 20 19.0123"
```

---

### Observaciones
Gracias al uso de la API externa, fue mucho más fácil realizar la conversión.

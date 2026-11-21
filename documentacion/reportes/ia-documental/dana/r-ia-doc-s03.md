# Reporte de Uso Documental en el Proyecto

**Fecha:** 14-11-2026
**Número de reporte:** 03 (Dana Carmona Santamaria)

---

# 1. Breve descripción de uso

Consultas para entender cómo funciona el manejo uniforme de errores, cómo se conecta el backend con el frontend, qué códigos de respuesta usa el balance del grupo y cómo funciona el servicio de tipo de cambio para los gastos en otra moneda.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 19-11-2026 | Claude | [Consulta 1: Funcionamiento del manejo de errores](#consulta-1-funcionamiento-del-manejo-de-errores) | [Respuesta 1](#respuesta-1) |
| 20-11-2026 | Claude | [Consulta 2: Conexión entre backend y frontend](#consulta-2-conexión-entre-backend-y-frontend) | [Respuesta 2](#respuesta-2) |
| 20-11-2026 | Claude | [Consulta 3: Códigos de respuesta del balance](#consulta-3-códigos-de-respuesta-del-balance) | [Respuesta 3](#respuesta-3) |
| 20-11-2026 | Claude | [Consulta 4: Funcionamiento del servicio de tipo de cambio](#consulta-4-funcionamiento-del-servicio-de-tipo-de-cambio) | [Respuesta 4](#respuesta-4) |

---

### Consulta 1: Funcionamiento del manejo de errores
> "¿Cómo funciona el manejo uniforme de errores de la API? ¿Qué hace cada componente y por qué los errores 401 y 403 se configuran en la seguridad y no en el manejador global?"

#### Respuesta 1
Cuando algo falla, el servicio lanza una excepción con un nombre claro. El `GlobalExceptionHandler` atrapa esa excepción, revisa qué código HTTP le corresponde y arma la respuesta con `ErrorResponse`, que tiene el código y el mensaje. Si hay errores en los datos, también incluye la lista de campos con `ErrorCampo`. Los errores 401 y 403 ocurren antes de llegar a los controladores, porque Spring Security revisa la petición primero. Por eso se configuran en `SecurityConfig`, que responde en el mismo formato JSON. Los errores inesperados responden 500 con un mensaje general, y el detalle se guarda solo en el registro del servidor.

---

### Consulta 2: Conexión entre backend y frontend
> "¿Cómo se conectan el backend y el frontend en este proyecto? ¿Dónde van los archivos JavaScript y CSS, y por qué se copian al compilar?"

#### Respuesta 2
Spring Boot entrega dos tipos de archivos. Las páginas HTML con Thymeleaf viven en `templates/` y las sirve el controlador de páginas. Los archivos JavaScript y CSS se sirven desde `static/`, y las rutas `/js/**` y `/css/**` los entregan al navegador. Los archivos que escribimos a mano están en `frontend/`, pero Spring no lee esa carpeta. Por eso `backend/pom.xml` tiene un bloque `<resources>` que copia `frontend/` a `static/` cada vez que se compila el proyecto. Así solo hay una fuente que editar. Cuando la página carga, sus scripts llaman a la API con `fetch()` (por ejemplo, `/api/viajes/{id}/balance`), el backend responde en JSON y el script pinta el resultado sin recargar la página. La sesión viaja en una cookie, por eso las peticiones usan `credentials: "same-origin"`.

---

### Consulta 3: Códigos de respuesta del balance
> "¿Qué códigos de respuesta devuelve el endpoint del balance del grupo (200, 401, 403 y 404) y cómo los usa la página para mostrar el resultado?"

#### Respuesta 3
El balance responde 200 cuando todo sale bien y regresa los saldos y las transferencias. Responde 401 cuando no hay sesión iniciada, y el mensaje de error hace que el usuario vuelva a la pantalla de login. Responde 403 cuando el usuario sí tiene sesión, pero no participa en el viaje, así que no se muestran sus datos. Responde 404 cuando el viaje no existe o fue cancelado. En la página, los errores 401, 403 y 404 se muestran con los avisos de `mensajes.js`, y dentro de la tarjeta del balance aparece el texto «No se pudo cargar el balance». Si no hay conexión con el servidor, la tarjeta muestra el mensaje de falla de red.

---

### Consulta 4: Funcionamiento del servicio de tipo de cambio
> "¿Qué hace Frankfurter, cómo funciona y cómo se usa para convertir un gasto en USD o EUR a la moneda del viaje?"

#### Respuesta 4
Frankfurter es un servicio gratuito, sin clave de acceso, que publica tipos de cambio de referencia del Banco Central Europeo. Las tasas se actualizan una vez al día en días hábiles, así que no son cotizaciones en tiempo real. Frankfurter no convierte montos: solo regresa cuántas unidades de una moneda valen una unidad de otra. Por eso la página pide la tasa, por ejemplo de USD a MXN, y multiplica el monto por esa tasa. El resultado se redondea a 2 decimales para el monto y se guarda la tasa con hasta 6 decimales, para que el gasto no cambie después. La versión actual es la 2 (`/v2/rate/USD/MXN`) y la versión 1 (`/latest?from=USD&to=MXN`) sigue disponible. Por eso el código intenta primero la 2 y, si falla, usa la 1. Si ambas fallan o no hay internet, el formulario avisa y deja capturar el monto en la moneda del viaje.

---

## Observaciones adicionales

Estas consultas me ayudaron a entender el orden en que Spring revisa una petición, cómo llegan los archivos del frontend al navegador, qué significa cada código de respuesta del balance y cómo funciona el servicio que usa el conversor de divisas.
# Reporte de Avance del Proyecto

**Equipo:** Bitácora de viajes colaborativa (Dana Carmona Santamaría, Stephanie Elizdeth Hernández Prieto, Diego Rafael Jiménez Trujano, Alfonso Mendoza Vásquez, Citlaly Morales Viveros)

**Fecha:** 22/11/2026

**Número de reporte:** 5

**Repositorio GitHub:** [https://github.com/Peps06/desarrollo-web](https://github.com/Peps06/desarrollo-web)

---

# 1. Actividades realizadas

- **Stephanie:** Ruta para editar un viaje (`/viajes/{id}/editar`) y gestión de participantes en el navegador: agregar por correo, quitar con confirmación (solo la organizadora) y lista con nombre, correo y rol. También el enlace «Editar» y el botón «Eliminar» del viaje en la lista, y el formulario que detecta la ruta de edición, carga los datos y guarda con `PUT`. Además el calendario de entregas y los criterios para dar por terminada una historia (`calendario.md` y `definition-of-done.md`).
- **Citlaly:** README completo, con arquitectura, equipo, estructura del repositorio, requisitos, base de datos, configuración, ejecución, qué debe funcionar, datos de prueba, catálogo de la API, pruebas, problemas comunes, seguridad y flujo de trabajo.
- **Diego:** Botón de voto conectado en las tarjetas de actividad: vota y quita el voto sin recargar la página, actualiza el contador y la marca de «Más votada». También las primeras capturas de evidencia del backend, la base de datos, las vistas y las pruebas en vivo.
- **Dana:** Plan de pruebas del sistema (`documentacion/pruebas.md`) con resultados reales: 13 pruebas unitarias en verde, flujo completo en el navegador y 16 casos de error por `cURL`. También correcciones al README, a la guía de CSS y a los criterios de terminado.
- **Alfonso:** Prueba de clonar y correr el proyecto en otro entorno WSL limpio, siguiendo solo el README, con bitácora y capturas de cada paso. Después ordenó la bitácora y las capturas en `documentacion/evidencias/por_acomodar/clonación/`.

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| :-------- | :--------------------- | :-------------- |
| Editar y eliminar viaje (ruta `/viajes/{id}/editar`) | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/2f3a33d27035ff9635dc38ec5cb417f40d170d23](https://github.com/Peps06/desarrollo-web/commit/2f3a33d27035ff9635dc38ec5cb417f40d170d23) |
| README completo (arquitectura, API, base de datos y pruebas) | Citlaly Morales Viveros | [https://github.com/Peps06/desarrollo-web/commit/7d190073bfcbeb999d72fde0e64f1fc878141316](https://github.com/Peps06/desarrollo-web/commit/7d190073bfcbeb999d72fde0e64f1fc878141316) |
| Participantes en el navegador y editar/eliminar viaje | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/3a3593e9c014f29df6abef28e95f0dc949f45731](https://github.com/Peps06/desarrollo-web/commit/3a3593e9c014f29df6abef28e95f0dc949f45731) |
| Votación en vivo conectada al detalle del viaje | Diego Rafael Jiménez Trujano | [https://github.com/Peps06/desarrollo-web/commit/69f4e4d0fa09c2fbeca9ab8b0ad8a6b656d77d97](https://github.com/Peps06/desarrollo-web/commit/69f4e4d0fa09c2fbeca9ab8b0ad8a6b656d77d97) |
| Pruebas listas | Dana Carmona Santamaría | [https://github.com/Peps06/desarrollo-web/commit/b1650385033f0802fe71a055b30f199fc4efedd8](https://github.com/Peps06/desarrollo-web/commit/b1650385033f0802fe71a055b30f199fc4efedd8) |
| Primeras capturas de evidencia | Diego Rafael Jiménez Trujano | [https://github.com/Peps06/desarrollo-web/commit/e7cfe793c9ac95640c65367380c5c68ba338e452](https://github.com/Peps06/desarrollo-web/commit/e7cfe793c9ac95640c65367380c5c68ba338e452) |
| Clonación y ejecución en otro entorno WSL | Alfonso Mendoza Vásquez | [https://github.com/Peps06/desarrollo-web/commit/9d534db14c2207ef89e479b010c79db21d9d5286](https://github.com/Peps06/desarrollo-web/commit/9d534db14c2207ef89e479b010c79db21d9d5286) |
| Acomodar la bitácora y las capturas de clonación | Alfonso Mendoza Vásquez | [https://github.com/Peps06/desarrollo-web/commit/b0d1134e8c1211adbef93b6d01b78e5a36f0fd23](https://github.com/Peps06/desarrollo-web/commit/b0d1134e8c1211adbef93b6d01b78e5a36f0fd23) |

---

# 3. Problemas encontrados

- **No existía `participantes.js` (Stephanie):** `detalle.html` cargaba ese archivo pero no existía, así que la página daba 404 y no había forma de agregar o quitar participantes desde el navegador. Se creó el archivo con la sección de participantes, el formulario por correo (solo la organizadora) y los botones «Quitar».
- **No había botón de voto en las tarjetas (Diego):** las tarjetas solo mostraban el texto «N votos» y `detalle.html` no cargaba `votos.js`. Se agregó el botón `.btn-voto` con su contador, se cargó `votos.js` y se escucha el evento `viaje:recargar` para refrescar la actividad más votada.
- **No había botones para editar o eliminar el viaje (Stephanie):** la ruta `/viajes/{id}/editar` existía pero nada enlazaba a ella y el formulario no la detectaba. Se agregaron el enlace «Editar» y el botón «Eliminar» en la lista (solo la organizadora) y el formulario ahora detecta la ruta, carga los datos y guarda con `PUT`.
- **MySQL de otra distribución usaba el puerto 3306 (Alfonso):** al probar en un WSL limpio, la instalación de MySQL fallaba porque otra distribución ya tenía MySQL encendido y comparten el puerto. Se agregó a «Problemas comunes» del README.
- **Hibernate no crea las tablas `actividad` y `gasto` en MySQL 8 (Alfonso):** en la distribución limpia, la aplicación no pudo crear esas dos tablas sola. El README ahora pide cargar `database/esquema.sql` antes del primer arranque.
- **El README pedía entrar a la carpeta antes de explicar cómo clonar (Alfonso):** la sección «Configuración local» estaba antes que la clonación. La clonación pasó a una sección propia antes de la configuración.
- **Caso 13 del plan de pruebas (Dana):** la categoría iba en minúscula (`"transporte"`) y el servidor devolvió `400` por JSON inválido antes de revisar la moneda, así que ese caso no probó lo que decía. Se corrigió a `"TRANSPORTE"` y quedó pendiente volver a correrlo.
- **Los participantes agregados por correo salían con el nombre «Dana» (Dana):** no era un error del código. Esas cuentas se registraron con ese nombre, y el sistema muestra el nombre con el que se registró cada cuenta.

---

# 4. Avance funcional

**Operativas / completadas (módulos de Dana):**

- Plan de pruebas del sistema con resultados reales: 13 pruebas unitarias en verde (`BUILD SUCCESS`), flujo completo de 12 pasos en el navegador y 16 casos de error por `cURL` con su resultado.
- Correcciones al README (nombre de Citlaly y línea de `frontend/`), a la guía de CSS (los estilos viven en `frontend/css/`) y a los criterios de terminado.

**Operativas / completadas (módulos de Stephanie):**

- Gestión de participantes en el detalle del viaje (`#zona-participantes`): lista con nombre, correo y rol, formulario por correo solo para la organizadora y botones «Quitar» con confirmación (la organizadora no se puede quitar a sí misma).
- Editar viaje (`/viajes/{id}/editar`): el formulario detecta la ruta, carga los datos con `GET /api/viajes/{id}`, cambia el título a «Editar viaje» y guarda con `PUT`. Eliminar viaje desde la lista con confirmación (solo la organizadora).
- Calendario de entregas (`documentacion/calendario.md`) y criterios para dar por terminada una historia (`documentacion/definition-of-done.md`).

**Operativas / completadas (módulos de Diego):**

- Botón de voto en cada tarjeta de actividad (`.btn-voto` con contador y estado `votado`), que vota o quita el voto sin recargar y refresca la actividad más votada de cada día.
- Capturas de evidencia del backend, la base de datos, las vistas y las pruebas en vivo (`documentacion/evidencias/02-backend/`, `03-mysql/`, `04-vistas/` y `05-en-vivo/`).

**Operativas / completadas (módulos de Citlaly):**

- README completo: arquitectura en 3 capas, equipo, estructura del repositorio, requisitos, base de datos con las 7 tablas, configuración, ejecución, qué debe funcionar, datos de prueba, catálogo de la API, pruebas, problemas comunes, seguridad y flujo de trabajo.

**Operativas / completadas (módulos de Alfonso):**

- Clonación y ejecución en otro entorno WSL limpio (Ubuntu 22.04): el proyecto se pudo correr siguiendo solo el README, con tres problemas del README ya corregidos. Bitácora con la tabla de pasos, la verificación final y la lista de capturas (`documentacion/evidencias/por_acomodar/clonación/`).

---

# 5. Uso de IA generativa

- **Alfonso**

- **Herramienta utilizada:** Claude Code (Anthropic), extensión de Visual Studio Code, modelo Claude Sonnet 5.5
- **Actividad para la que se utilizó:** Entender el README y el proyecto antes de probarlo en una distribución de WSL completamente limpia. La IA no ejecutó la prueba ni modificó la distribución; Alfonso corrió todos los comandos a mano.
- **Código o contenido generado:** `documentacion/evidencias/por_acomodar/clonación/bitacora.md` (tabla de pasos para llenar, consultas externas, verificación final y lista de 17 capturas) y los comandos de PowerShell para crear y borrar la distribución nueva.
- **Modificaciones realizadas por el equipo:** La IA solo explicó cada paso del README (20 pasos) y entregó una lista de problemas probables sin corregir nada. Los tres problemas del README se corrigieron a mano después de la prueba.
- **Forma en que se verificó el resultado:** El proyecto corrió en la distribución limpia siguiendo solo el README (backend arranca, tablas en MySQL, inicio de sesión, votar y registrar gasto desde el navegador). El detalle está en `documentacion/reportes/ia-desarrollo/alfonso/r-ia-dev-s06.md`.

---

# 6. Participación individual

- **Dana Carmona Santamaría:** Hizo el plan de pruebas del sistema y registró los resultados reales (13 pruebas unitarias en verde, flujo en el navegador y 16 casos de error por `cURL`). Corrigió el README, la guía de CSS y los criterios de terminado, y revisó la votación, los participantes y la documentación del equipo.
- **Stephanie Elizdeth Hernández Prieto:** Hizo la gestión de participantes en el navegador y el editar y eliminar viaje, con la ruta, los botones y el formulario que carga los datos. También el calendario de entregas y los criterios para dar por terminada una historia.
- **Citlaly Morales Viveros:** Completó el README con arquitectura, API, modelo de datos y pruebas.
- **Diego Rafael Jiménez Trujano:** Conectó el botón de voto en las tarjetas de actividad y subió las primeras capturas de evidencia del backend, la base, las vistas y las pruebas en vivo.
- **Alfonso Mendoza Vásquez:** Probó clonar y correr el proyecto en otro WSL limpio, encontró y corrigió tres problemas del README y ordenó la bitácora con sus capturas.

---

# 7. Próximas actividades

- Volver a correr el caso 13 del plan de pruebas (moneda con la categoría en mayúscula) y registrar la persistencia.
- Ensayo de la demo con cronómetro y verificación final con clonación limpia, según el calendario.

---

## Observaciones adicionales
- Durante esta etapa se realizaron las pruebas necesarias para determinar errores y corregirlos correctamente.

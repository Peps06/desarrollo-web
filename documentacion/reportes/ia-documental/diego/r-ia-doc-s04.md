# Reporte de Uso Documental en el Proyecto

**Fecha:** 22-11-2026
**Número de reporte:** 04 (Diego Rafael Jiménez Trujano)

---

# 1. Breve descripción de uso

Consultas para el cierre del entregable 5: revisión del estado del proyecto contra el backlog de historias de usuario, reparto de los pendientes por responsable y orden de urgencia de los de Diego, apoyo para plantear y revisar los cambios de la votación en vivo (HU-30), y recopilación de las evidencias del proyecto (HU-43): estructura de carpetas, capturas necesarias, comandos para obtenerlas en WSL, MySQL y el navegador, solicitud de la evidencia de otro WSL (HU-42), revisión y organización de las capturas, y redacción del índice de evidencias.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 22-11-2026 | Claude | [Consulta 1: Revisión del estado del proyecto](#consulta-1-revisión-del-estado-del-proyecto) | [Respuesta 1](#respuesta-1) |
| 22-11-2026 | Claude | [Consulta 2: Pendientes por responsable](#consulta-2-pendientes-por-responsable) | [Respuesta 2](#respuesta-2) |
| 22-11-2026 | Claude | [Consulta 3: Pendientes de Diego por urgencia](#consulta-3-pendientes-de-diego-por-urgencia) | [Respuesta 3](#respuesta-3) |
| 22-11-2026 | Claude | [Consulta 4: Prompts para implementar la votación en vivo](#consulta-4-prompts-para-implementar-la-votación-en-vivo) | [Respuesta 4](#respuesta-4) |
| 22-11-2026 | Claude | [Consulta 5: Revisión de cambios y mensaje de commit](#consulta-5-revisión-de-cambios-y-mensaje-de-commit) | [Respuesta 5](#respuesta-5) |
| 22-11-2026 | Claude | [Consulta 6: Pendientes después de los commits](#consulta-6-pendientes-después-de-los-commits) | [Respuesta 6](#respuesta-6) |
| 22-11-2026 | Claude | [Consulta 7: Cómo probar la votación](#consulta-7-cómo-probar-la-votación) | [Respuesta 7](#respuesta-7) |
| 22-11-2026 | Claude | [Consulta 8: Plan para reunir las evidencias del proyecto](#consulta-8-plan-para-reunir-las-evidencias-del-proyecto) | [Respuesta 8](#respuesta-8) |
| 22-11-2026 | Claude | [Consulta 9: Ubicación y organización de las capturas](#consulta-9-ubicación-y-organización-de-las-capturas) | [Respuesta 9](#respuesta-9) |
| 22-11-2026 | Claude | [Consulta 10: Error al ejecutar Maven en PowerShell](#consulta-10-error-al-ejecutar-maven-en-powershell) | [Respuesta 10](#respuesta-10) |
| 22-11-2026 | Claude | [Consulta 11: Acceso a MySQL y capturas de la base de datos](#consulta-11-acceso-a-mysql-y-capturas-de-la-base-de-datos) | [Respuesta 11](#respuesta-11) |
| 22-11-2026 | Claude | [Consulta 12: Pestaña Network y caso de error](#consulta-12-pestaña-network-y-caso-de-error) | [Respuesta 12](#respuesta-12) |
| 22-11-2026 | Claude | [Consulta 13: Solicitud de la evidencia de otro WSL](#consulta-13-solicitud-de-la-evidencia-de-otro-wsl) | [Respuesta 13](#respuesta-13) |
| 22-11-2026 | Claude | [Consulta 14: Revisión y renombrado de capturas](#consulta-14-revisión-y-renombrado-de-capturas) | [Respuesta 14](#respuesta-14) |
| 22-11-2026 | Claude | [Consulta 15: Índice de evidencias](#consulta-15-índice-de-evidencias) | [Respuesta 15](#respuesta-15) |

---

### Consulta 1: Revisión del estado del proyecto
> "Revisa el archivo de historias de usuario (Excel) y el estado actual del repositorio del proyecto, y elabora un diagnóstico de lo que falta por completar. El equipo ya identificó los siguientes pendientes; verifícalos y complétalos con tu propio análisis:
>
> 1. **HU-30, votación en vivo (Diego):** `votos.js` existe, pero `detalle.html` no lo carga y las tarjetas muestran «N votos» como texto, sin botón. Faltan agregar el `<script>` en `detalle.html` (sin sobrescribir el archivo, que ya incluye `participantes.js` en la línea 110), sustituir el `span` de votos por un botón con contador en `viaje-detalle.js` (líneas 415-418) y escuchar el evento `viaje:recargar` para que la insignia «Más votada» se actualice. La validación consiste en que dos usuarios voten la misma actividad.
> 2. **HU-39, comprobantes (Citlaly):** la subida y la visualización funcionan desde `gastos.js`, pero `comprobantes.js` es un archivo de una sola línea; debe completarse o eliminarse, dejando documentado dónde está la lógica. Verifica, además, que al editar o eliminar un gasto se recalcule el presupuesto."

#### Respuesta 1
Se revisaron el Excel de historias de usuario (con su pestaña de requerimientos), el árbol del proyecto, el repositorio y los reportes. Se confirmó lo que indicaban: `votos.js` estaba completo, pero `detalle.html` no lo cargaba, el `span` «N votos» seguía en `viaje-detalle.js` y ningún archivo escuchaba `viaje:recargar`. En cuanto a HU-39, `comprobantes.js` no existía en el repositorio y la lógica de comprobantes estaba en `gastos.js`; el recálculo del presupuesto al editar o eliminar un gasto ya estaba cableado, porque `gastos.js` emite `gasto-registrado` y `presupuesto.js` y `balance.js` lo escuchan. Además se detectaron pendientes que nadie había mencionado: no había interfaz de participantes (`participantes.js` no existía aunque `detalle.html` lo cargaba), las fases de pruebas y documentación (HU-40 a HU-44) estaban casi vacías, el Excel no coincidía con la realidad (HU-30 figuraba como «Hecho» y de Alfonso) y el árbol del proyecto tenía rutas desactualizadas (`src/` y `docs/` en lugar de `backend/`, `frontend/` y `documentacion/`). También había dos versiones del Excel en el proyecto, de las cuales la más antigua tenía casi todo en «Pendiente».

---

### Consulta 2: Pendientes por responsable
> "Con base en el diagnóstico anterior, presenta nuevamente los pendientes indicando el integrante responsable de cada uno, según el Excel de historias de usuario y el árbol del proyecto."

#### Respuesta 2
Se presentó una tabla con cada pendiente y su responsable, tomado del Excel y del árbol del proyecto: HU-30 para Diego (el Excel decía Alfonso); HU-39 y el recálculo de gastos para Citlaly; HU-40 para Dana; HU-42 y HU-44 para Alfonso; HU-43 para Diego. Los pendientes sin dueño claro (la interfaz de participantes, la actualización del árbol y el alta de historias en el Excel) se marcaron como propuestas, con Stephy como responsable sugerida por mantener el backlog y la documentación.

---

### Consulta 3: Pendientes de Diego por urgencia
> "Filtra la lista anterior y presenta únicamente los pendientes asignados a Diego, ordenados por urgencia."

#### Respuesta 3
Se ordenaron así: (1) conectar la votación (script, botón y listener); (2) probar con dos usuarios; (3) corregir la fila de HU-30 en el Excel; (4) resolver la línea 110 de `detalle.html`, que apuntaba a un archivo inexistente; y (5) las evidencias de HU-43, dejadas al final porque las capturas de votación solo sirven cuando la funcionalidad ya opera.

---

### Consulta 4: Prompts para implementar la votación en vivo
> "Redacta un prompt para delegar la implementación de los pendientes de Diego a otro chat del proyecto, siguiendo el formato y el nivel de detalle de los prompts incluidos en los reportes de IA de desarrollo de Diego.
>
> Una vez revisado el primer cambio, redacta un segundo prompt que resuelva las dos observaciones identificadas en la revisión del código antes de realizar el commit."

#### Respuesta 4
Se leyeron los reportes de IA de desarrollo anteriores para copiar su estructura (rol, contexto, reglas, restricciones y entregables) y se redactó un primer prompt para conectar la votación (script, botón con contador y listener de `viaje:recargar`) con los datos de archivos, líneas y funciones verificados en el repositorio. Después de revisar el primer cambio se redactó un segundo prompt para dos observaciones: agregar un ícono fijo al botón, porque solo mostraba un número, y confirmar que el `aria-label` debía quedarse fijo, ya que `aria-pressed` comunica el estado. Ambos prompts quedaron registrados en el reporte de IA de desarrollo 04.

---

### Consulta 5: Revisión de cambios y mensaje de commit
> "Revisa los cambios realizados en el repositorio y, si son correctos, proporciona el mensaje de commit correspondiente.
>
> Revisa nuevamente la implementación de las correcciones y, si es correcta, proporciona el mensaje de commit."

#### Respuesta 5
Se revisó el diff real del repositorio: `detalle.html` solo tenía una línea nueva (`votos.js`) y la de `participantes.js` seguía intacta; `viaje-detalle.js` tenía el botón con contador, el ícono 👍 con `aria-hidden`, el listener de `viaje:recargar` y sin la función `textoVotos`, que ya no se usaba. Se comprobó que los saltos de línea CRLF se conservaron y que el archivo pasaba la verificación de sintaxis con Node. Con ello se propuso un mensaje de commit (`HU-30: Votación en vivo conectada al detalle del viaje`), junto con la indicación de que la prueba con usuarios seguía pendiente.

---

### Consulta 6: Pendientes después de los commits
> "Revisa el estado actual del repositorio e indica qué actividades quedan pendientes para Diego."

#### Respuesta 6
Se actualizó la lista con el estado real del repositorio: el commit de HU-30 ya estaba hecho y subido, `participantes.js` ya existía y se cargaba en el detalle (se revisó que su sección propia no se sobrescribe cuando el detalle se repinta), y quedaban pendientes la prueba en el navegador, la actualización de la fila de HU-30 en el Excel, el reporte de IA y las evidencias de HU-43. También se señaló un detalle ajeno a Diego: `participantes.js` no emite `viaje:recargar` al agregar o quitar participantes, por lo que el balance puede quedar desfasado hasta recargar la página.

---

### Consulta 7: Cómo probar la votación
> "Describe el procedimiento para probar la funcionalidad de votación en vivo, indicando cómo iniciar la aplicación, qué usuarios y datos de prueba existen y qué resultados se esperan."

#### Respuesta 7
Se explicó cómo arrancar la aplicación en WSL (`sudo service mysql start` y `./mvnw spring-boot:run`) y cómo probar con los datos de `DataLoader` (Ana, Luis y el viaje 1 de Oaxaca con actividades y votos previos): ver el botón de voto, quitar y volver a dar el voto, comprobar que la insignia «Más votada» cambia sin recargar, probar el doble clic, revisar la consola del navegador y, como alternativa, comprobar los casos de error con `Invoke-WebRequest` (voto duplicado, quitar sin voto previo y votar sin sesión).

---

### Consulta 8: Plan para reunir las evidencias del proyecto
> "Estoy elaborando el entregable 5 del proyecto «Bitácora de viajes colaborativa» (HU-43, Evidencias del proyecto), del que soy responsable. Necesito un plan para reunir las evidencias que pide la historia: (1) la estructura de carpetas del repositorio donde guardar las capturas, tomando como base `documentacion/evidencias/`; (2) la lista de capturas a tomar, agrupadas por tema conforme a los criterios de aceptación (instalación y versiones en WSL, backend corriendo desde la terminal, conexión a MySQL con `SHOW TABLES` y consultas, vistas principales, votación y gasto en vivo con la pestaña Network del navegador, evidencias de Git y GitHub, y clonación en otro WSL correspondiente a HU-42); (3) los comandos exactos con los que obtener cada captura; y (4) una convención de nombres descriptivos que permita referenciarlas desde un índice en el README. Considera que cada integrante me entrega las evidencias de sus historias y que el entorno de desarrollo es WSL con Ubuntu."

#### Respuesta 8
Se leyeron los criterios de aceptación de HU-43 en el Excel y el contenido de `documentacion/evidencias/`. Se propusieron siete carpetas por tema (`01-instalacion`, `02-backend`, `03-mysql`, `04-vistas`, `05-en-vivo`, `06-git` y `07-otro-wsl`) y la convención `tema-descripcion.png`. Se listaron las capturas por tema: versiones de Java, Maven, MySQL y Git; el arranque de la aplicación y `./mvnw test`; `SHOW TABLES` y consultas a las tablas principales; las vistas principales; la votación y el gasto con la pestaña Network; `git log --oneline --graph --all`, `git shortlog -sn` y los pull requests; y la evidencia de otro WSL. Se advirtió que el Excel menciona `docs/evidencias`, pero la carpeta real del repositorio es `documentacion/evidencias`, y que el criterio de las ramas `feature/hu-XX` debía comprobarse contra lo que existiera realmente en GitHub.

---

### Consulta 9: Ubicación y organización de las capturas
> "¿Dónde debo guardar las capturas de pantalla de las evidencias y cómo conviene organizarlas? ¿Es posible compartirlas contigo para que las clasifiques y las renombres?"

#### Respuesta 9
Se indicó que las capturas se pueden dejar en una carpeta temporal dentro del repositorio (`documentacion/evidencias/por_acomodar/`) para que la herramienta las abra una por una, identifique qué muestra cada una, las renombre con la convención y las mueva a su carpeta, o bien adjuntarlas en el chat si son pocas. También se advirtió que debían revisarse antes de subirse al repositorio para que no incluyeran contraseñas, credenciales de `application.properties` ni datos personales.

---

### Consulta 10: Error al ejecutar Maven en PowerShell
> "Al ejecutar `mvn -version` en Windows PowerShell obtengo el error «El término 'mvn' no se reconoce como nombre de un cmdlet», y lo mismo con `mvnw` (en la misma terminal, `java -version` muestra la versión 23.0.2). ¿A qué se debe y cómo debo ejecutar los comandos para la captura de instalación?"

#### Respuesta 10
Se explicó que la terminal usada era Windows PowerShell y no WSL: Maven solo está instalado dentro de WSL y Java en Windows era la versión 23, mientras que el proyecto usa Java 17, por lo que esa captura no servía como evidencia de instalación. Además, PowerShell no ejecuta archivos de la carpeta actual sin el prefijo `.\`. Se indicó abrir una terminal de Ubuntu (WSL), ejecutar ahí `java -version`, `mvn -version`, `mysql --version` y `git --version` en una sola captura con el prompt visible, y arrancar la aplicación desde `/mnt/c/Users/.../backend` con `./mvnw spring-boot:run`.

---

### Consulta 11: Acceso a MySQL y capturas de la base de datos
> "¿Cómo accedo a la base de datos MySQL desde la terminal para ejecutar las consultas de la sección de evidencias 03?
>
> ¿Qué consultas debo ejecutar y qué captura debo tomar para la sección 03 de evidencias?"

#### Respuesta 11
Se indicó, tomando los datos de `application.properties` (base `bitacora_viajes`, usuario `bitacora_app`), iniciar MySQL con `sudo service mysql start` y entrar con `sudo mysql bitacora_viajes` o con `mysql -u bitacora_app -p bitacora_viajes`. Para la captura se recomendaron `SHOW TABLES;` (con las 7 tablas) y consultas a `usuario` (solo `id, nombre, correo`, para no mostrar el hash de la contraseña), `viaje`, `voto` y `gasto` (con columnas seleccionadas para que no se parta la salida), tomadas después de la prueba de votación para que se vean los datos nuevos.

---

### Consulta 12: Pestaña Network y caso de error
> "Para la sección 05 de evidencias, ¿cómo puedo consultar el código de estado de las peticiones (por ejemplo, 200) si no logro abrir la pestaña Network con F12?
>
> Ya tengo acceso a la pestaña Network. ¿Cómo capturo la evidencia del registro de un gasto y de un caso de error?"

#### Respuesta 12
Se indicaron las formas de abrir las herramientas de desarrollo (F12, Fn + F12, Ctrl + Shift + I, clic derecho e Inspeccionar o el menú del navegador) y cómo encontrar la pestaña Network si estaba oculta bajo la flecha «»: filtrar por Fetch/XHR, marcar Preserve log, abrir la pestaña antes de realizar la acción y revisar la petición en Headers (método y *Status Code*) y en Response (JSON). Para el gasto se indicó que el estado correcto es 201. Para el caso de error se advirtió que la validación del formulario ocurre en JavaScript antes de enviar la petición, por lo que un monto inválido no genera ninguna petición; se propusieron errores que provienen del servidor: un 404 abriendo `/viajes/999999` y un 403 con un usuario que no participa en el viaje.

---

### Consulta 13: Revisión y renombrado de capturas
> "Revisa, renombra y organiza en las carpetas de evidencias las capturas de la carpeta temporal, incluidas las que envió Alfonso, e indica si cumplen su propósito como evidencia.
>
> Valida si las capturas de la votación en la pestaña Network son suficientes como evidencia.
>
> Organiza las capturas faltantes que agregué a la carpeta temporal."

#### Respuesta 13
Se abrieron y revisaron una por una las capturas y se renombraron y movieron a su carpeta de tema. En la revisión se detectó que una captura de MySQL mostraba la columna `contrasena_hash` con los hashes de las contraseñas, por lo que no se subió al repositorio y se sustituyó por una nueva con solo las columnas seguras; también que una captura del arranque se cortaba antes de la línea `Started BitacoraViajesApplication`, que la votación no mostraba el método HTTP en Network (se recomendó activar la columna Method) y que algunas capturas estaban en el puerto 8081 y las de la prueba limpia en el 8080. Las capturas de votación posteriores se validaron como evidencia útil: muestran la petición `votos` con iniciador `votos.js`, el repintado del detalle y el cambio de la insignia «Más votada». También se corrigió una afirmación anterior: votar devuelve 201 y quitar el voto 200, lo cual se confirmó en `VotoController`. Con permiso de borrado se eliminó la captura con hashes y la carpeta temporal.

---

### Consulta 14: Índice de evidencias
> "Redacta el índice de evidencias (`documentacion/evidencias/README.md`) con una tabla por tema. En la sección de la prueba en otro WSL, incorpora el siguiente resumen proporcionado por Alfonso: el proyecto sí corre en una distribución limpia siguiendo el README, pero el README tenía tres problemas que ya se corrigieron. (1) La instalación de MySQL falla si otra distribución de WSL tiene MySQL encendido, porque comparten el puerto 3306; se agregó el caso a «Problemas comunes». (2) «Configuración local» pedía entrar a la carpeta del repositorio antes de explicar cómo clonarlo; la clonación se pasó a una sección propia antes de la configuración. (3) La aplicación no puede crear las tablas `actividad` y `gasto` en MySQL 8; ahora el README indica cargar `database/esquema.sql` antes del primer arranque."

#### Respuesta 14
Se redactó `documentacion/evidencias/README.md` como índice de evidencias, con una tabla por tema que enlaza cada captura y explica qué demuestra, y se verificó que todos los enlaces apuntaran a archivos existentes. En la sección de la prueba en otro WSL se incluyeron el entorno usado (Ubuntu 22.04, Maven 3.6.3 y MySQL 8.0.46, con la aplicación en el puerto 8080) y los tres problemas del README que se corrigieron: el conflicto del puerto 3306 cuando otra distribución tiene MySQL encendido, el orden de las secciones de clonación y configuración, y la necesidad de cargar `database/esquema.sql` antes del primer arranque porque en MySQL 8 la aplicación no podía crear las tablas `actividad` y `gasto`. La sección de Git quedó marcada como pendiente hasta contar con las capturas de `git log`, `git shortlog` y los pull requests.

---

## Observaciones adicionales

Las respuestas se contrastaron con el repositorio y con las capturas reales (diff de los commits, `VotoController`, `application.properties`, historial de Git y el contenido de cada imagen). El detalle de los cambios de código está en el reporte de IA de desarrollo 04. Pendiente al cierre de este reporte: las capturas de Git y GitHub de la sección 06 de las evidencias, que dependen de que se suban los últimos cambios del equipo.

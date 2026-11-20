# Reporte de Uso Documental en el Proyecto

**Fecha:** 20-11-2026   
**Número de reporte:** 04 (Stephanie Elizdeth Hernández Prieto)  

---

# 1. Breve descripción de uso

Consultas de diagnóstico técnico, resolución de errores HTTP y protocolo en entorno local WSL, y aclaración de alcance funcional entre historias de backend y frontend para el módulo de viajes.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 20-11-2026 | Gemini | [Consulta 1: Error 404 en formato JSON al abrir `/viajes` en el navegador](#consulta-1-error-404-en-formato-json-al-abrir-viajes-en-el-navegador) | [Respuesta 1](#respuesta-1) |
| 20-11-2026 | Gemini | [Consulta 2: Error `NoPluginFoundForPrefixException` al ejecutar Maven desde la raíz](#consulta-2-error-nopluginfoundforprefixexception-al-ejecutar-maven-desde-la-raíz) | [Respuesta 2](#respuesta-2) |
| 20-11-2026 | Gemini | [Consulta 3: Excepción `Invalid character found in method name [0x160x030x01...]` en Tomcat](#consulta-3-excepción-invalid-character-found-in-method-name-0x160x030x01-en-tomcat) | [Respuesta 3](#respuesta-3) |
| 20-11-2026 | Gemini | [Consulta 4: Ubicación del botón para editar viajes dentro de las vistas del sistema](#consulta-4-ubicación-del-botón-para-editar-viajes-dentro-de-las-vistas-del-sistema) | [Respuesta 4](#respuesta-4) |

---

### Consulta 1: Error 404 en formato JSON al abrir `/viajes` en el navegador
> "Al entrar a `http://localhost:8085/viajes` la pantalla sale vacía con `{"codigo":404,"mensaje":"El recurso no existe"}`, aunque todo mi código está en la rama `main`."

#### Respuesta 1
Al revisar `PaginaController.java` se detectó que estaban mapeadas las rutas `/`, `/login`, `/registro`, `/viajes/nuevo` y `/viajes/{id}`, pero faltaba el método `@GetMapping("/viajes")` que retorna la plantilla `"viajes/lista"`. Al no existir ruta MVC registrada, Spring delegaba la petición al manejador de recursos estáticos y `GlobalExceptionHandler` devolvía la respuesta `404` en JSON. Se añadió el mapeo `@GetMapping("/viajes")` y se resolvió el acceso.

---

### Consulta 2: Error `NoPluginFoundForPrefixException` al ejecutar Maven desde la raíz
> "Al ejecutar `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8085"` me aparece `BUILD FAILURE: No plugin found for prefix 'spring-boot' in the current project`."

#### Respuesta 2
El comando se ejecutó desde el directorio raíz del repositorio (`/mnt/c/Users/Stephanie/desarrollo-web`), donde no existe ningún archivo `pom.xml`. Se indicó ingresar primero a la subcarpeta `backend/` (`cd backend`) antes de invocar `mvn spring-boot:run`.

---

### Consulta 3: Excepción `Invalid character found in method name [0x160x030x01...]` en Tomcat
> "Al abrir la aplicación en el navegador, la consola de Spring Boot lanza `java.lang.IllegalArgumentException: Invalid character found in method name [0x160x030x01...]`."

#### Respuesta 3
Los bytes `0x16 0x03 0x01` corresponden al encabezado binario del saludo TLS/SSL cuando el navegador intenta conectarse mediante `https://` a un servidor Tomcat local configurado únicamente para HTTP plano. Se solucionó accediendo explícitamente con `http://localhost:8085/login` y `http://localhost:8085/viajes`.

---

### Consulta 4: Ubicación del botón para editar viajes dentro de las vistas del sistema
> "¿En la vista de lista de viajes (`/viajes`) debería existir un botón para editar viajes existentes?"

#### Respuesta 4
Se revisó el catálogo de requerimientos y los wireframes del proyecto, confirmando que la historia de edición y cancelación de viajes (`PUT /api/viajes/{id}` y `DELETE /api/viajes/{id}`) corresponde exclusivamente a la capa de backend, mientras que la vista de lista de viajes únicamente contempla las tarjetas navegables hacia `/viajes/{id}`, los filtros por destino/rol y el botón «Crear viaje».

---

## Observaciones adicionales

Estas consultas permitieron diagnosticar con rapidez incidencias de configuración de rutas MVC, directorio de ejecución de Maven y protocolo HTTP en el entorno local WSL, manteniendo la separación de responsabilidades definida por el equipo.
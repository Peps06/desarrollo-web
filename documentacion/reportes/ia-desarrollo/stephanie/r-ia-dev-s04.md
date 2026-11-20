# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 20-11-2026  
**Número de reporte:** 04 (Stephanie Elizdeth Hernández Prieto - Formulario de crear viaje y Lista de viajes)  

---

# 1. Breve descripción de uso

Apoyo en la construcción, estilizado, integración con `fetch()` y depuración de las vistas frontend para **Formulario de crear viaje** (`/viajes/nuevo`) y **Lista de viajes** (`/viajes`), respetando los fragmentos Thymeleaf (`fragments/base.html`), la guía de clases CSS del equipo (`componentes.css` y `base.css`) y el contrato REST de los endpoints `POST /api/viajes` y `GET /api/viajes` con filtros combinables por destino y rol.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Gemini |
| **Modelo/temperatura** | No aplica |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `e5ecb26`) |
| **Código posterior** | Commit `HEAD` de Formulario de crear viaje y Lista de viajes |
| **Diferencias entre versiones** | Ver salida de `git diff --numstat` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | Pruebas manuales en navegador (`http://localhost:8085/viajes/nuevo` y `http://localhost:8085/viajes`): validación de campos, creación con redirección `201`, filtrado dinámico sin recarga por destino/rol, distinción visual de insignias y estado vacío |
| **Código de uso** | Generación / Modificación / Depuración |
| **Reflexión del estudiante** | Comprendí cómo desacoplar el renderizado de plantillas Thymeleaf de las operaciones asíncronas con `fetch()`, aplicar *debounce* en búsquedas en tiempo real para evitar saturar la API, manejar respuestas HTTP `400`/`401` sin perder los datos capturados en el formulario y mantener sincronizados los archivos estáticos entre `frontend/` y `src/main/resources/static/` en Spring Boot. |

---

### Prompt original
> "Ayúdame a construir las vistas frontend y el JavaScript para el Formulario de crear viaje (`/viajes/nuevo`) y la Lista de viajes (`/viajes`) usando Thymeleaf, las clases de `guia-css.md` y `componentes.css` de nuestro equipo, y `fetch()` contra `POST /api/viajes` y `GET /api/viajes` con filtros dinámicos por destino y rol, incluyendo estado vacío y manejo de errores."

### Respuesta de IA
La herramienta generó la estructura completa de presentación e interacción para ambas vistas:
1. **`fragments/base.html` y `PaginaController.java`:** Configuración de cabecera común con fuentes (`Fraunces` y `Source Sans 3`), hojas de estilo compartidas, barra de navegación `.barra-nav` y rutas MVC `@GetMapping("/viajes")` y `@GetMapping("/viajes/nuevo")`.
2. **`viajes/nuevo.html` y `viaje-nuevo.js`:** Formulario con validación instantánea en cliente (destino obligatorio, fecha de fin mayor o igual a inicio, presupuesto mayor a cero y moneda `MXN`/`USD`/`EUR`), envío JSON vía `POST /api/viajes`, mapeo de errores `400` junto a cada campo sin borrar lo capturado y redirección a `/viajes/{id}` al recibir `201 Created`.
3. **`viajes/lista.html` y `viajes-lista.js`:** Cuadrícula responsiva (`.rejilla`) con tarjetas clicables hacia `/viajes/{id}`, distinción visual entre organizador (`.insignia.insignia-organizador`) y participante (`.insignia`), barra de búsqueda por destino con *debounce* (250 ms) y selector de rol mediante `GET /api/viajes?destino=...&rol=...` sin recargar la página, además del estado vacío `.vacio` («Aún no tienes viajes»).

### Diferencias entre versiones
```text
# Ejecutar en WSL con los hashes reales de tus commits antes de cerrar el archivo:
# git diff --numstat <commit-anterior> <commit-lista-viajes>
backend/src/main/java/com/bitacora/viajes/controller/PaginaController.java
backend/src/main/resources/templates/fragments/base.html
backend/src/main/resources/templates/viajes/nuevo.html
backend/src/main/resources/templates/viajes/lista.html
backend/src/main/resources/static/js/viaje-nuevo.js
backend/src/main/resources/static/js/viajes-lista.js
frontend/js/viaje-nuevo.js
frontend/js/viajes-lista.js
# Reporte de Avance del Proyecto

**Equipo:** Bitácora de viajes colaborativa (Dana Carmona Santamaría, Stephanie Elizdeth Hernández Prieto, Diego Rafael Jiménez Trujano, Alfonso Mendoza Vásquez, Citlaly Morales Viveros)  
**Fecha:** ‹dd/mm/aaaa›  
**Número de reporte:** 3  
**Repositorio GitHub:** [https://github.com/Peps06/desarrollo-web](https://github.com/Peps06/desarrollo-web)

---

# 1. Actividades realizadas

Durante esta fase avanzamos en el desarrollo de **Backend completo (Entregable 3: API REST completa)**.

- Se creó un manejo de errores común para toda la API. Todas las respuestas de error tienen el mismo formato, con un código y un mensaje. Cuando hay errores en los datos, también se muestra la lista de campos con problemas.
- Los errores de inicio de sesión y de permisos (401 y 403) también responden en el mismo formato, en lugar de responder sin contenido.
- Los errores inesperados responden con un mensaje general, sin mostrar detalles internos.
- *(Espacio para que los demás integrantes agreguen sus actividades)*

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| :--- | :--- | :--- |
| Manejo uniforme de errores HTTP y formato único de respuesta | Dana Carmona Santamaría | [https://github.com/Peps06/desarrollo-web/commit/e5f4e](https://github.com/Peps06/desarrollo-web/commit/e5f4e8e965130cc7c9cd91d068bfefdc7105c63e) |
| *(Espacio para actividades de los demás integrantes)* | | |

---

# 3. Problemas encontrados

- **Respuestas 401 en todas las pruebas:** Las pruebas con cURL respondían 401 aunque la petición fuera válida. La causa fue que el archivo de cookies no tenía una sesión activa. Se resolvió iniciando sesión otra vez antes de repetir las pruebas.
- *(Espacio para los problemas de los demás integrantes)*

---

# 4. Avance funcional

**Operativas / completadas (módulos de Dana):**

- Formato único de error en toda la API, con código, mensaje y lista de campos cuando aplica.
- Respuestas 401 y 403 en formato JSON desde la configuración de seguridad.
- Respuesta 405 para métodos no permitidos y 404 para rutas que no existen.

**Operativas / completadas (módulos de los demás integrantes):**

- *(Espacio para completar)*

**Pendientes:**

- *(Espacio para completar)*

---

# 5. Uso de IA generativa

-**Dana**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Diseño del manejo común de errores, configuración de respuestas 401 y 403 en la seguridad, y revisión del formato de respuesta.
- **Código o contenido generado:** Formato de error, manejador global de errores, cambios en la configuración de seguridad y cambios en las llamadas de error de los controladores.
- **Modificaciones realizadas por el equipo:** Se realizó en manejo completo y unificado para todos los erres existentes, ya no están separados localmente.
- **Forma en que se verificó el resultado:** Pruebas con cURL y revisión del formato de cada respuesta. El detalle está en `documentacion/reportes/ia-desarrollo/` y en `documentacion/reportes/ia-documental/`.

---

-*(Espacio para el uso de IA de los demás integrantes)*

---

# 6. Participación individual

- **Stephanie Elizdeth Hernández Prieto:** *(Completar)*
- **Dana Carmona Santamaría:** Creó el manejo común de errores HTTP y el formato de respuesta de toda la API. Configuró las respuestas 401 y 403 en JSON y revisó las respuestas de error con pruebas de cURL.
- **Diego Rafael Jiménez Trujano:** *(Completar)*
- **Alfonso Mendoza Vásquez:** *(Completar)*
- **Citlaly Morales Viveros:** *(Completar)*

---

# 7. Próximas actividades

- Actividad 1: Revisar que los endpoints de los demás integrantes respondan con el formato de error común.
- *(Espacio para próximas actividades del resto del equipo)*

---

## Observaciones adicionales

- *(Espacio para observaciones del equipo)*

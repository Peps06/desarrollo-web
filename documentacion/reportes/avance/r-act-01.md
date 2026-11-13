# Reporte de Avance del Proyecto

**Equipo:** Bitácora de viajes colaborativa (Dana Carmona Santamaría, Stephanie Elizdeth Hernández Prieto, Diego Rafael Jiménez Trujano, Alfonso Mendoza Vásquez, Citlaly Morales Viveros)  
**Fecha:** 10/11/2026  
**Número de reporte:** 1  
**Repositorio GitHub:** [https://github.com/Peps06/desarrollo-web](https://github.com/Peps06/desarrollo-web)

---

# 1. Actividades realizadas

Durante la primera semana completamos la fase de **Análisis y diseño (Entregable 1: Diseño inicial)**:

- Se creó y configuró el repositorio de GitHub con los 5 integrantes como colaboradores, flujo de trabajo Git definido  y archivo `.gitignore` .
- Se redactaron los requerimientos funcionales y no funcionales, se validó el tema "Bitácora de viajes colaborativa" con el profesor y se construyó el backlog priorizado con las historias de usuario asignadas por responsable, prioridad y puntos.
- Se diseñó el diagrama Entidad-Relación con las 7 tablas del sistema: usuario, viaje, participante\_viaje, actividad, voto, gasto y tarea, con llaves primarias, foráneas y restricciones únicas.
- Se elaboraron los wireframes de todas las vistas (login/registro, lista de viajes, crear viaje, detalle con tarjetas por día, votación, gastos con selector de moneda, barra de presupuesto, balance de deudas, checklist y modal de comprobante) junto con la guía de clases CSS compartidas.

---

# 2. Evidencias de trabajo


| Actividad                                                                   | Integrante responsable | Enlace a commit                                                                                                    |
| --------------------------------------------------------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------ |
| Creación y configuración del repositorio GitHub, README y flujo de trabajo. | Alfonso Mendoza        | [https://github.com/Peps06/desarrollo-web/commit/bc41a94](https://github.com/Peps06/desarrollo-web/commit/81a510d) |
| Requerimientos funcionales y no funcionales + backlog priorizado.           | Stephanie Hernández    | [https://github.com/Peps06/desarrollo-web/commit/32be6f3](https://github.com/Peps06/desarrollo-web/commit/81a510d) |
| Diagrama Entidad-Relación de las 7 tablas                                   | Dana Carmona           | [https://github.com/Peps06/desarrollo-web/commit/81a510d](https://github.com/Peps06/desarrollo-web/commit/115162d) |
| Wireframes de las vistas y guía de clases CSS                               | Citlaly Morales        | [https://github.com/Peps06/desarrollo-web/commit/599d9bd](https://github.com/Peps06/desarrollo-web/commit/599d9bd) |
|                                                                             | Diego Jiménez          | [https://github.com/Peps06/desarrollo-web/commit/ca37260](https://github.com/Peps06/desarrollo-web/commit/81a510d) |


---

# 3. Problemas encontrados

Durante esta semana no se presentaron problemas técnicos relevantes. El trabajo se repartió por historias de usuario y cada integrante trabajó individualmente, por lo que no hubo conflictos entre cambios.

---

# 4. Avance funcional

**Operativas / completadas (documentación y diseño):**

- Repositorio GitHub configurado con flujo de trabajo Git.
- Requerimientos, tema validado y backlog priorizado.
- Diagrama Entidad-Relación de las 7 tablas.
- Wireframes de las vistas y guía CSS.

**Pendientes (inician la próxima fase):**

- Guía de instalación y verificación del entorno WSL: JDK 17, Maven, MySQL y Git.
- Catálogo de endpoints REST con contratos de petición/respuesta.
- Proyecto Spring Boot, entidades JPA y conexión a MySQL, primera historia de la fase de backend.

Aún no existe código ejecutable: corresponde a la fase de análisis y diseño; la primera versión funcional (API inicial) corresponde al siguiente entregable.

---

# 5. Uso de IA generativa

No se utilizó IA generativa durante esta semana del proyecto. El diseño, la documentación y el backlog fueron elaborados por el equipo.

---

# 6. Participación individual

- **Alfonso Mendoza Vásquez:** creación y configuración del repositorio GitHub, invitación de colaboradores, definición del flujo de trabajo Git y README inicial.
- **Stephanie Elizdeth Hernández Prieto:** redacción de requerimientos, gestión de la validación del tema con el profesor, backlog priorizado y tablero de GitHub Projects.
- **Dana Carmona Santamaría:** diseño del diagrama Entidad-Relación de las 7 tablas con llaves, restricciones y reglas de borrado.
- **Diego Rafael Jiménez Trujano:** avance del catálogo de endpoints; apoyo en la revisión del diseño.
- **Citlaly Morales Viveros:** wireframes de las 10 vistas del sistema y guía de clases CSS compartidas.

---

# 7. Próximas actividades

- Actividad 1: Completar la guía de instalación del entorno WSL (JDK 17, Maven, MySQL, Git) y que cada integrante verifique su entorno.
- Actividad 2: Terminar y aprobar en equipo el catálogo de endpoints REST `docs/api.md`.
- Actividad 3: Crear el proyecto Spring Boot con las 7 entidades JPA, repositorios y conexión a MySQL.
- Actividad 4: Implementar registro de usuario e iniciar sesión segura.

---

## Observaciones adicionales

- Cada hito del calendario tendrá su release correspondiente en GitHub (V1, V2, ...), siendo V1 el correspondiente a esta fase de diseño.
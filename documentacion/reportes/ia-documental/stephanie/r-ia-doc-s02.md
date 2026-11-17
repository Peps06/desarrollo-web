# Reporte de Uso Documental en el Proyecto

**Fecha:** 16-11-2026   
**Número de reporte:** 02 (Stephanie Elizdeth Hernández Prieto)  

---

# 1. Breve descripción de uso

Consultas técnicas y de diagnóstico sobre validación de entidades JPA (`ParticipanteViaje` y `Usuario`), dependencias de permisos entre módulos (`VotoService` y `GastoService`), errores de sintaxis JPQL/HQL en Spring Data JPA e interpretación de respuestas HTTP `204 No Content` al probar con `cURL`.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 14-11-2026 | Gemini | [Consulta 1: Confirmación de entidades y dependencias para HU-18](#consulta-1-confirmación-de-entidades-y-dependencias-para-hu-18) | [Respuesta 1](#respuesta-1) |
| 14-11-2026 | Gemini | [Consulta 2: Errores de compilación en DTO y Service](#consulta-2-errores-de-compilación-en-dto-y-service) | [Respuesta 2](#respuesta-2) |
| 14-11-2026 | Gemini | [Consulta 3: Error de sintaxis HQL al iniciar Spring Boot](#consulta-3-error-de-sintaxis-hql-al-iniciar-spring-boot) | [Respuesta 3](#respuesta-3) |
| 14-11-2026 | Gemini | [Consulta 4: Interpretación de HTTP 204 sin cuerpo en DELETE](#consulta-4-interpretación-de-http-204-sin-cuerpo-en-delete) | [Respuesta 4](#respuesta-4) |

---

### Consulta 1: Confirmación de entidades y dependencias para HU-18
> "para que estes mas seguro te paso esa informacion y te digo que no me debes dar codigo sin antes confirmamrme y decirme si te falta algun dato si ya no falta mas a hora si dime que haremos"

#### Respuesta 1
Se confirmó que `ParticipanteViaje` cuenta con la restricción única `uq_viaje_usuario` (`viaje_id`, `usuario_id`) y los métodos `getViaje()`, `getUsuario()` y `getRol()`, que `UsuarioRepository` dispone de `findByCorreo(String correo)`[cite: 2], y que tanto `VotoService` (HU-14) como `GastoService` (HU-15) validan la pertenencia activa en `participante_viaje` lanzando `403` si el usuario fue eliminado[cite: 3], mientras sus gastos previos se conservan por la regla `ON DELETE RESTRICT` de MySQL.

---

### Consulta 2: Errores de compilación en DTO y Service
> "antes de hacer las pruebas, me marcan algunos erres en los codigos en PartiicpanteService me marcar error en verificarOrganizador y en particpanteResponse me mearca error en"

#### Respuesta 2
En `ParticipanteResponse` el error ocurría al invocar `getUsuarioId().getId()` en lugar de navegar la relación `@ManyToOne` mediante `participacion.getUsuario().getId()`, `getNombre()` y `getCorreo()`[cite: 2, 4]. En `ParticipanteService` el error se debía a una llave de cierre faltante en `obtenerViajeError` que dejaba anidado `verificarOrganizador` y a una discrepancia en el nombre de la variable `organizador`[cite: 5, 6, 7].

---

### Consulta 3: Error de sintaxis HQL al iniciar Spring Boot
> "al correr spring me salio esto: ... Caused by: org.hibernate.query.SyntaxException: At 1:61 and token '.', mismatched input '.', expecting one of the following tokens ... [Select p FROM ParticipanteViaje p JOIN FETCH p.usuarioWhere p.viaje.id = :viajeId AND p.usuario.id = :usuarioId]"

#### Respuesta 3
La excepción ocurría porque al concatenar la consulta JPQL en `ParticipanteViajeRepository` faltó un espacio en blanco entre `JOIN FETCH p.usuario` y `WHERE`, generando el token inválido `p.usuarioWhere`. Al agregar el espacio antes de `WHERE` y guardar el archivo, Hibernate validó correctamente la consulta al iniciar el contexto.

---

### Consulta 4: Interpretación de HTTP 204 sin cuerpo en DELETE
> "estas son las pruebas que no me convencen checa si estan bien y dime que pasa (`DELETE /api/viajes/1/participantes/3 -> HTTP/1.1 204`)"

#### Respuesta 4
Se explicó que `HTTP/1.1 204 No Content` es la respuesta esperada según la convención REST definida en `api.md` para eliminaciones exitosas sin cuerpo de respuesta JSON, confirmando que el participante con ID `3` fue removido correctamente del viaje[cite: 1, 8].

---

## Observaciones adicionales

Estas consultas permitieron integrar los endpoints de gestión de participantes (`GET`, `POST` y `DELETE /api/viajes/{id}/participantes`) sin modificar entidades ni servicios creados por el resto del equipo, asegurando compatibilidad con las reglas de seguridad de HU-14 y HU-15[cite: 1, 3].
# Reporte de Uso Documental en el Proyecto

**Fecha:** 12-11-2026   
**Número de reporte:** 01 (Stephanie Elizdeth Hernández Prieto)  

---

# 1. Breve descripción de uso

Consultas teóricas y de diagnóstico sobre configuración de CORS en Spring Boot con sesiones basadas en cookies y comportamiento de códigos de estado HTTP (`302` vs `401`) en Spring Security.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 12-11-2026 | Gemini | [Consulta 1: Uso de @CrossOrigin](#consulta-1-uso-de-crossorigin) | [Respuesta 1](#respuesta-1) |
| 12-11-2026 | Gemini | [Consulta 2: Redirección 302 en lugar de 401](#consulta-2-redirección-302-en-lugar-de-401) | [Respuesta 2](#respuesta-2) |

---

### Consulta 1: Uso de @CrossOrigin
> "¿A este controlador no se le pone la anotación `@CrossOrigin` como en los ejemplos de clase?"

#### Respuesta 1
No se debe agregar `@CrossOrigin` porque tanto las vistas Thymeleaf como los endpoints `/api/**` se sirven desde el mismo origen (`localhost:8080`) y utilizan cookies de sesión (`JSESSIONID`), a diferencia de los ejemplos de clase donde el frontend y el backend corrían en puertos distintos.

---

### Consulta 2: Redirección 302 en lugar de 401
> "Al probar `GET /api/viajes` sin sesión con `cURL`, el servidor respondió `HTTP/1.1 302` redirigiendo a `/login` en lugar de `401 Unauthorized`, ¿por qué ocurre esto?"

#### Respuesta 2
Ocurre porque en `SecurityConfig.java` el método `.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))` está declarado después de `.defaultAuthenticationEntryPointFor(...)`, lo que sobrescribe la respuesta `401` configurada para `/api/**` y redirige todas las peticiones sin autenticar hacia `/login`.

---

## Observaciones adicionales

Estas consultas permitieron confirmar que la arquitectura del controlador era correcta sin modificar archivos de configuración compartidos del equipo, y reportar el hallazgo de seguridad a la compañera responsable de `SecurityConfig.java`.
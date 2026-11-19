# Reporte de Uso Documental en el Proyecto

**Fecha:** 14-11-2026
**Número de reporte:** 03 (Dana Carmona Santamaria)

---

# 1. Breve descripción de uso

Consulta para entender cómo funciona el manejo uniforme de errores: qué componentes intervienen, en qué orden se ejecutan y por qué algunos errores se configuran en la seguridad.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 14-11-2026 | Claude | [Consulta 1: Funcionamiento del manejo de errores](#consulta-1-funcionamiento-del-manejo-de-errores) | [Respuesta 1](#respuesta-1) |

---

### Consulta 1: Funcionamiento del manejo de errores
> "¿Cómo funciona el manejo uniforme de errores de la API? ¿Qué hace cada componente y por qué los errores 401 y 403 se configuran en la seguridad y no en el manejador global?"

#### Respuesta 1
Cuando algo falla, el servicio lanza una excepción con un nombre claro. El `GlobalExceptionHandler` atrapa esa excepción, revisa qué código HTTP le corresponde y arma la respuesta con `ErrorResponse`, que tiene el código y el mensaje. Si hay errores en los datos, también incluye la lista de campos con `ErrorCampo`. Los errores 401 y 403 ocurren antes de llegar a los controladores, porque Spring Security revisa la petición primero. Por eso se configuran en `SecurityConfig`, que responde en el mismo formato JSON. Los errores inesperados responden 500 con un mensaje general, y el detalle se guarda solo en el registro del servidor.

---

## Observaciones adicionales

Esta consulta me ayudó a entender el orden en que Spring revisa una petición y qué parte de la API se encarga de cada tipo de error.

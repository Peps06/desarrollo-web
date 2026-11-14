# Reporte de Uso Documental en el Proyecto

**Fecha:** 14-11-2026
**Número de reporte:** 02 (Dana Carmona Santamaria - Manejo uniforme de errores HTTP)

---

# 1. Breve descripción de uso

Consultas para entender por qué algunas respuestas de error salen del manejador global y otras no, y por qué los errores de seguridad necesitan configuración especial.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 14-11-2026 | Claude | [Consulta 1: Respuestas 401 en todas las pruebas](#consulta-1-respuestas-401-en-todas-las-pruebas) | [Respuesta 1](#respuesta-1) |
| 14-11-2026 | Claude | [Consulta 2: Explicación completa](#consulta-2-explicación-completa) | [Respuesta 2](#respuesta-2) |

---

### Consulta 1: Respuestas 401 en todas las pruebas
> "Al probar la API con cURL todas las peticiones dan 401, menos una. ¿Es un error del código?"

#### Respuesta 1
No es un error del código. El archivo de cookies no tenía una sesión válida, y Spring revisa la sesión antes de revisar el id o el cuerpo. Al iniciar sesión otra vez, las pruebas responden según lo esperado.

---

### Consulta 2: Explicación completa
> "Dame una explicación completa de como se hicieron los cambios y como funciona ahora"

#### Respuesta 2
Con el manejo uniforme de errores, todos los errores de la API responden con la misma forma: un código HTTP y un mensaje que puede mostrarse al usuario. Cuando hay datos inválidos, también se incluye la lista de campos con problemas. Antes cada controlador manejaba sus errores de forma distinta, así que ahora un solo manejador global decide el código y el mensaje, y los errores de sesión y de permisos (401 y 403) se configuran en la seguridad para responder con el mismo formato. Los errores inesperados responden un mensaje general, sin detalles internos, y el detalle técnico queda solo en el registro del servidor. La lógica de negocio y los datos no cambiaron; solo cambió la forma en que la API informa cuando algo sale mal.


## Observaciones adicionales

Estas consultas me ayudaron a entender el orden en que Spring revisa una petición y para que sirvieron los cabios realizados..

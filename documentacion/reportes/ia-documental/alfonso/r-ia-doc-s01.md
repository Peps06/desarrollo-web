# Reporte de Uso Documental en el Proyecto

**Fecha:** 13-11-2026
**Número de reporte:** 01 (Alfonso Mendoza Vásquez)

---

# 1. Breve descripción de uso

Consultas para preparar el entorno WSL con MySQL y para entender cómo se valida un voto, cómo se calcula un presupuesto sin errores de redondeo y cómo se prueba un servicio con JUnit.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 13-11-2026 | Claude | [Consulta 1: Instalar MySQL y crear la base en WSL](#consulta-1-instalar-mysql-y-crear-la-base-en-wsl) | [Respuesta 1](#respuesta-1) |
| 13-11-2026 | Claude | [Consulta 2: Error bad interpreter en mvnw](#consulta-2-error-bad-interpreter-en-mvnw) | [Respuesta 2](#respuesta-2) |
| 13-11-2026 | Claude | [Consulta 3: Orden de las validaciones del voto](#consulta-3-orden-de-las-validaciones-del-voto) | [Respuesta 3](#respuesta-3) |
| 13-11-2026 | Claude | [Consulta 4: BigDecimal en el cálculo del presupuesto](#consulta-4-bigdecimal-en-el-cálculo-del-presupuesto) | [Respuesta 4](#respuesta-4) |

---

### Consulta 1: Instalar MySQL y crear la base en WSL
> "Hola Claude, como sé si mi WSL está configurado para el proyecto, cómo corro la base de datos, en el wsl de mi powershell? Y como creo la base? Si puedes hazme un tutorial facil."

#### Respuesta 1
Se revisó el WSL: tenía Java 17, Maven y Git, pero no MySQL. Se indicó instalarlo con `apt`, encenderlo con `sudo service mysql start` (en WSL no arranca solo), crear la base `bitacora_viajes` y el usuario `bitacora_app` desde `sudo mysql`, y copiar `application.properties.example` a `application.properties` con la contraseña.

---

### Consulta 2: Error bad interpreter en mvnw
> "Al correr `./mvnw spring-boot:run` sale `bad interpreter: /bin/sh^M: No such file or directory`. Qué pasó"

#### Respuesta 2
El script tenía saltos de línea de Windows (CRLF) porque el repositorio se clonó desde Windows, y Linux no los puede ejecutar. Se corrigió con una regla local de git (`mvnw text eol=lf`) para no modificar archivos del repositorio. Además, para crear las tablas `actividad` y `gasto` en MySQL 8.4 se usó `database/esquema.sql`, porque Hibernate generaba un `DEFAULT CURRENT_TIMESTAMP` que MySQL rechaza.

---

### Consulta 3: Orden de las validaciones del voto
> "En que orden debo validar al votar? Que pasaría si dos peticiones llegan al mismo tiempo?"

#### Respuesta 3
Primero se busca la actividad (404 si no existe), luego se verifica que el usuario participe en el viaje de esa actividad (403) y al final se revisa si ya votó (400). Si dos peticiones llegaran a la vez, la restricción `UNIQUE (actividad_id, usuario_id)` de MySQL rechaza el segundo voto aunque ambas hayan pasado la validación del servicio.

---

### Consulta 4: BigDecimal en el cálculo del presupuesto
> "por que debo usar bigdecimal? cómo evito la división entre cero y los errores de redondeo en el porcentaje?"

#### Respuesta 4
`double` tiene errores de representación (0.1 + 0.2 no da exactamente 0.3), por eso con dinero se usa `BigDecimal`. El porcentaje se calcula multiplicando por 100 antes de dividir y redondeando a 2 decimales con `RoundingMode.HALF_UP`. No hay división entre cero porque el presupuesto siempre es mayor a 0 (lo validan HU-10 y una restricción `CHECK` de la base), y si no hay gastos la suma se trata como 0.

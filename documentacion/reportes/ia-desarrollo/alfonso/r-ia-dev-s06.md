# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 22-11-2026
**Número de reporte:** 06 (Alfonso Mendoza Vásquez - Clonación y ejecución en otro entorno WSL)

---

# 1. Breve descripción de uso

Uso de la IA como guía para entender el README y el proyecto antes de probarlo en una distribución de WSL completamente limpia (HU-42). La IA no ejecutó la prueba ni modificó la distribución; yo corrí todos los comandos a mano. Antes de la prueba, la IA leyó el `README.md`, el `pom.xml`, `application.properties.example`, `DataLoader`, `SecurityConfig`, `mvnw` y la guía `guia-instalacion-wsl.md`. Con eso me explicó en lenguaje sencillo qué hace cada paso del README (20 pasos, del 0 al 19), por qué hace falta y qué podía salir mal, y me entregó una lista de problemas probables sin corregir nada.

También creó `documentacion/evidencias/por_acomodar/clonación/bitacora.md` con la tabla de pasos para llenar, la sección de consultas externas, la verificación final (backend arranca, tablas en MySQL, inicio de sesión, votar y registrar gasto desde el navegador) y la lista de 17 capturas con nombre de archivo sugerido. Además me dio los comandos de PowerShell para crear una distribución nueva (`Ubuntu-hu42`) y para borrarla, con la advertencia de que `wsl --unregister` es irreversible.

Resultado de la prueba (hecha por mí): el proyecto corrió en una distribución limpia siguiendo solo el README, con tres problemas que ya se corrigieron en el README: (1) la instalación de MySQL falla si otra distribución de WSL tiene MySQL encendido, porque comparten el puerto 3306 (se agregó a "Problemas comunes"); (2) "Configuración local" pedía entrar a la carpeta del repositorio antes de explicar el clonado, por lo que la clonación pasó a una sección propia antes de la configuración; (3) la aplicación no puede crear las tablas `actividad` y `gasto` en MySQL 8, y ahora el README indica cargar `database/esquema.sql` antes del primer arranque.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude Code (Anthropic), extensión de VS Code |
| **Modelo/temperatura** | Claude Sonnet 5.5 (Anthropic) |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `b1650385033f0802fe71a055b30f199fc4efedd8` (Pruebas listas) |
| **Código posterior** | Commit `9d534db14c2207ef89e479b010c79db21d9d5286` (Clonación y ejecución en otro entorno WSL) |
| **Diferencias entre versiones** | [Ver diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Explicación y documentación (no se generó código Java, HTML, JS ni CSS) |
| **Reflexión del estudiante** | Aprendí que un proyecto que funciona en mi computadora puede depender de cosas que ya tengo instaladas o configuradas, y que la única forma de saberlo es probarlo en un entorno vacío. Entendí qué hace cada herramienta del README: `apt` instala programas, MySQL guarda los datos y hay que encenderlo a mano en WSL, `mvnw` descarga su propio Maven y Spring Boot crea las tablas al arrancar. Vi que el orden de las secciones importa: una instrucción correcta puede fallar si va antes de otra de la que depende. También aprendí que dos distribuciones de WSL comparten el puerto 3306, y que por eso MySQL de una puede estorbar a la otra. Lo utlimo fue que Hibernate no crea las tablas `actividad` y `gasto` en MySQL 8, es un problema del código y no del README; cargar `esquema.sql` lo evita, pero el equipo debería corregir el mapeo de las entidades. Además, anotar cada ayuda como consulta externa vuelve la prueba honesta: si necesité ayuda en un paso, el README no era suficiente ahí. |

---

### Prompt original
> Soy Alfonso, voy a trabajar la HU-42: Clonación y ejecución en otro entorno WSL (depende de HU-41, el README final de Citlaly). Quiero que primero entiendas cómo vamos a trabajar.
>
> Objetivo de la HU: demostrar que el proyecto corre en una distribución de WSL limpia (sin nada instalado) siguiendo ÚNICAMENTE el README. Cada paso faltante o incorrecto se anota y se corrige en el README. Se guarda evidencia (capturas o video corto) de la ejecución limpia.
>
> Cómo vamos a trabajar:
> - Tu papel es ayudarme a ENTENDER el README y el proyecto. Yo ejecuto TODOS los comandos a mano en la distribución limpia, tal cual dice el README. Tú no ejecutas la prueba, no instalas nada en esa distribución y no corriges el README antes de que yo termine de probarlo.
> - Antes de ejecutar, explícame en lenguaje sencillo qué hace cada paso del README y por qué hace falta. Asume que tengo poca experiencia con WSL, Linux y MySQL.
> - Cuando algo falle, yo te pego el error y lo que corrí. Tú me explicas qué significa, por qué pasó y cómo arreglarlo, y qué cambio debería llevar el README. Yo aplico y verifico la corrección.
> - Si te hago una consulta durante la prueba, anótala como "consulta externa" en la bitácora.
> - Si no puedes verificar algo en el código o en el README, dímelo; no inventes comandos, versiones ni resultados.
>
> PASO 1 - Preparación: 1. Lee docs/evidencias/README.md, el README.md del proyecto, el pom.xml, application.properties.example y DataLoader. 2. Resumen de qué le pide el README a una máquina limpia, paso a paso. 3. Lista de problemas probables del README, sin corregir nada. 4. Crea docs/evidencias/hu-42/bitacora.md (tabla de pasos, consultas externas, verificación final, lista de capturas). 5. Comandos de PowerShell para crear y borrar una distribución nueva de Ubuntu, con advertencia de lo que borra.
>
> Fuera de alcance: no ejecutes la prueba limpia ni toques la distribución; no corrijas el README todavía; no cambies código Java, plantillas, JS ni CSS. Git: no hagas commits, ramas ni push.

### Respuesta de IA
- Aclaró que la ruta real es `documentacion/evidencias/` (no `docs/evidencias/`) y que `documentacion/evidencias/README.md` solo tiene un comentario de una línea (índice de HU-43), por lo que no define ningún formato.
- Entregó el resumen de 20 pasos del README (instalar paquetes, comprobar Java, iniciar MySQL, crear base y usuario, clonar, copiar y editar `application.properties`, `chmod +x mvnw`, `./mvnw spring-boot:run`, verificación de tablas y usuarios, inicio de sesión, `curl` de login y `./mvnw test`) con qué hace cada uno y qué puede fallar.
- Lista de 14 problemas probables, sin corregir nada. Los principales: el orden de las secciones (la configuración local va antes del clonado), el clonado por SSH que falla en una máquina limpia, no explica el usuario de Linux ni cómo editar el archivo, la segunda terminal puede abrir otra distribución, y el problema `/nonexistent` de MySQL solo está en la guía de WSL. Indicó qué no podía verificar (por ejemplo, que `localhost:8080` abra desde Windows, o que `sudo mysql` entre sin contraseña).
- Creó `bitacora.md` en `documentacion/evidencias/por_acomodar/clonación/` y dio los comandos de PowerShell (`wsl --install -d Ubuntu-24.04 --name Ubuntu-hu42`, `wsl --terminate` y `wsl --unregister`) con la advertencia de que borrar la distribución es irreversible y de que la opción `--name` depende de la versión de WSL.
- Al informarle los resultados de la prueba, señaló que el hallazgo de las tablas `actividad` y `gasto` es un problema del código y no del README, que la sección "Base de datos" podía quedar contradictoria con "las tablas las genera la aplicación", y que cada ayuda recibida debe anotarse como consulta externa.

### Diferencias entre versiones
```text
21	11	README.md
(nuevo)	documentacion/evidencias/por_acomodar/clonación/bitacora.md   (95 líneas)
```

---

### Pruebas
La prueba fue manual, hecha por mí en la distribución limpia, siguiendo solo el README. La evidencia está en `documentacion/evidencias/por_acomodar/clonación/` (`bitacora.md`, `capturas/` y `logs/`).

| # | Verificación | Resultado esperado | Resultado obtenido |
| - | ------------ | ------------------ | ------------------ |
| 1 | El backend arranca | Consola con `Started BitacoraViajesApplication` | OK: `Tomcat started on port 8080` y `Started BitacoraViajesApplication` (después de cargar `esquema.sql`; el primer arranque falló, ver Falla 3) |
| 2 | Las tablas existen en MySQL | `SHOW TABLES;` muestra las 7 tablas | OK: 7 tablas y 2 usuarios de prueba |
| 3 | Inicio de sesión con usuario de prueba | `ana@example.com` / `Password123` entra desde el navegador | OK: entra a "Mis viajes" y abre el detalle del viaje 1 |
| 4 | Votar una actividad desde el navegador | El contador cambia sin recargar | OK: el contador pasó de 1 a 0 y de 0 a 1 sin recargar (`DELETE` 200 / `POST` 201 con `fetch`) |
| 5 | Registrar un gasto desde el navegador | El gasto aparece en la lista | OK: el gasto de 350 MXN aparece en la lista y el total gastado pasa a $5,375.00 MXN |
| 6 | Pruebas automáticas | `./mvnw test`: `BUILD SUCCESS`, `Tests run: 13` | OK: `Tests run: 13, Failures: 0, Errors: 0`, `BUILD SUCCESS` |

Problemas encontrados y corregidos en el README: puerto 3306 compartido entre distribuciones, orden de las secciones clonación y configuración, y carga de `database/esquema.sql` antes del primer arranque por las tablas `actividad` y `gasto` en MySQL 8.

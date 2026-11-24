# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 22-11-2026
**Número de reporte:** 08 (Dana Carmona Santamaria)

---

## 1. Breve descripción de uso

Uso ia para preparar el plan de pruebas del sistema. Yo pedí las pruebas de cURL para comprobar los errores de la API sin depender del navegador.

---

## 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `69f4e4d0fa09c2fbeca9ab8b0ad8a6b656d77d97`|
| **Código posterior** | Commit `b1650385033f0802fe71a055b30f199fc4efedd8` |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#pruebas) |
| **Código de uso** | Pruebas |
| **Reflexión del estudiante** | [PENDIENTE: escribir en tus palabras] |

---

### Prompt original
> "Ayúdame a preparar las pruebas del sistema. Necesito que me indiques qué tipos de pruebas debo realizar para comprobar que todo funciona. En particular, ayúdame a generar los comandos cURL para probar los casos de error de los endpoints de la API, con su resultado esperado."


### Respuesta de IA
Claude entregó una preparación (usuarios semilla Ana y Luis, viaje de Oaxaca), las 13 pruebas unitarias y un caso de error por cada endpoint con su comando cURL listo para pegar. Se entregaron pruebas por cada tipo de usuario y de codigos de error en especifico, como 400, 401, 403 y 404.

### Diferencias entre versiones
```
git diff --numstat 69f4e4d0fa09c2fbeca9ab8b0ad8a6b656d77d97 b1650385033f0802fe71a055b30f199f
c4efedd8
2       2       README.md
1       1       documentacion/definition-of-done.md
1       1       documentacion/guia-css.md
105     1       documentacion/pruebas.md
```

### Pruebas

```
# Pruebas completas registradas en documentacion/pruebas.md.

BASE=http://localhost:8081
dana@Dana:~/desarrollo-web$ curl -c cookies-ana.txt -H "Content-Type: application/json" -d '{"correo":"ana@example.com","contrasena":"Password123"}' $BASE/api/auth/login

dana@Dana:~/desarrollo-web$ curl -c cookies-luis.txt -H "Content-Type: application/json" -d '{"correo":"luis@example.com","contrasena":"Password123"}' $BASE/api/auth/login
{"id":2,"nombre":"Luis Pérez","correo":"luis@example.com"}

dana@Dana:~/desarrollo-web$ curl -b cookies-ana.txt $BASE/api/viajes
[{"id":1,"destino":"Oaxaca, Oaxaca","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":15000.00,"monedaBase":"MXN","rol":"ORGANIZADOR"}]

```

---

## Observaciones adicionales
La versión recortada del plan deja un caso por cada tipo de error, para no probar lo mismo varias veces. La votación ya quedó conectada al detalle del viaje en `69f4e4d` (Diego, 22 nov 2026): el botón de voto aparece en cada actividad y el paso 6 del flujo ya se puede hacer en el navegador. El flujo completo en el navegador (sección 3 del plan) se completó el 22 nov 2026 y todo funcionó. El plan de pruebas ya está en el repositorio (commit `b165038`).

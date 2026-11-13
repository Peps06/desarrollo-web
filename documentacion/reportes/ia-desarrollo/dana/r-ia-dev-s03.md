# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 11-11-2026
**Número de reporte:** 03 (Dana Carmona Santamaria - Seguridad base y datos de prueba)

---

# 1. Breve descripción de uso

Creación de `SecurityConfig` con un codificador BCrypt y reglas provisionales, y del `DataLoader` que carga datos de prueba solo si la base de usuarios está vacía.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `d2c3123bc9c6ad7eef8e477ee9004c9805526dcd` (Conversion y accesos a la base) |
| **Código posterior** | Commit `aa9594aaed7a1f90fc2d3f96c92d9446962f7166` (Base de datos lista) |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#prueba) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Con estos archivos realice la forma que responde el servidor cuando hay sesión activa, cuando falta esta sesión y la forma de guardar las contaseñas cifradas con BCrypt.Todo esto para que se realice la conexión de forma segura y al arrancar se carguen los datos de prueba que agregue. |

---

### Prompt original
> "Genera SecurityConfig con un bean BCryptPasswordEncoder, csrf deshabilitado y permitAll en todas las rutas, indicando que es provisional."

> "Genera el DataLoader con estos datos de prueba, usando PasswordEncoder y los repositorios, sin duplicar datos si ya existen."

### Respuesta de IA
Se devolvió una clase `@Configuration` con el bean `PasswordEncoder` de BCrypt y un `SecurityFilterChain` que permite todas las peticiones. También se devolvió un componente que implementa `CommandLineRunner`. Revisa si ya hay usuarios y, si no hay, guarda 2 usuarios, 1 viaje, 2 actividades con 3 votos, 2 gastos y 2 tareas.

### Diferencias entre versiones
```text
156	1	backend/src/main/java/com/bitacora/viajes/config/DataLoader.java
12	0	backend/src/main/java/com/bitacora/viajes/BitacoraViajesApplication.java
27	1	backend/src/main/java/com/bitacora/viajes/config/SecurityConfig.java
2	2	backend/src/main/java/com/bitacora/viajes/repository/ActividadRepository.java
```

---

### Prueba
```
mysql -u bitacora_app -p bitacora_viajes -e "SELECT id, correo, LEFT(contrasena_hash, 7) AS hash FROM usuario;"
Enter password:
+----+--------------------+---------+
| id | correo             | hash    |
+----+--------------------+---------+
|  1 | ana@example.com    | $2a$10$ |
|  2 | luis@example.com   | $2a$10$ |
|  3 | diego@example.com  | $2a$10$ |
|  4 | diego2@example.com | $2a$10$ |
+----+--------------------+---------+
``` 

## Observaciones adicionales

En este commit también se corrigió `ActividadRepository`, porque tenía el contenido de `VotoRepository` y el proyecto no compilaba. La clase principal `BitacoraViajesApplication` también se agregó aquí. La configuración de seguridad es provisional y se reemplazó en el reporte 05.

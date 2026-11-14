# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 11-11-2026
**Número de reporte:** 01 (Dana Carmona Santamaria - Revisión del esquema SQL)

---

# 1. Breve descripción de uso

Revisión de la versión base de `database/esquema.sql` contra el diagrama ER. Se agregaron restricciones, llaves foráneas, valores por defecto y tipos ENUM. No se agregaron tablas ni columnas.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `7d16c68138eec3b023e8b694b13fd2e5c29157f9` (Base inicial) |
| **Código posterior** | Commit `f7a4da89be2cc0206bab008aedeaa14385885a50` (Esquema base listo) |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#prueba) |
| **Código de uso** | Revisión |
| **Reflexión del estudiante** | Durante esta sesión, confirme y agregue algunas partes para el funcionamiento de mi base de datos, la ia me ayudo a agegar llaves, restricciones y valores necesarios para el funcionamiento del proyecto. |

---

### Prompt original
> "Revisa este esquema.sql de MySQL 8 contra el diagrama ER. Mejora lo necesario sin agregar tablas ni columnas que no estén en el diagrama. Explica cada cambio."

### Respuesta de IA
Se devolvió el archivo completo. Agrega `NOT NULL` según el diagrama, valores por defecto, `ENUM` para estado, rol y categoría, llaves foráneas con nombre, restricciones `UNIQUE`, cuatro `CHECK` y `ON DELETE CASCADE` solo en `voto`. Cada cambio se explicó.

### Diferencias entre versiones
```text
88	50	database/esquema.sql
```

---

### Prueba 
```
mysql -u bitacora_app -p bitacora_viajes -e "SHOW TABLES;"
Enter password:
+---------------------------+
| Tables_in_bitacora_viajes |
+---------------------------+
| actividad                 |
| gasto                     |
| participante_viaje        |
| tarea                     |
| usuario                   |
| viaje                     |
| voto                      |
+---------------------------+
```

## Observaciones adicionales

El archivo no lo ejecuta Spring Boot. Las tablas las crea Hibernate con `ddl-auto=update`, así que este archivo sirve para comparar con `DESCRIBE`.

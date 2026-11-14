# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 11-11-2026
**Número de reporte:** 02 (Dana Carmona Santamaria - Entidades JPA y repositorios)

---

# 1. Breve descripción de uso

Creación de las 7 entidades JPA con sus 3 enums, y de los 7 repositorios `JpaRepository` del paquete `com.bitacora.viajes`.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `f7a4da89be2cc0206bab008aedeaa14385885a50` (Esquema base listo) |
| **Código posterior** | Commit `d2c3123bc9c6ad7eef8e477ee9004c9805526dcd` (Conversion y accesos a la base) |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Durante esta parte, realice y comprendí el funcionamiento las clases de Java que sirven para definir la base de datos, sus llaves y relaciones. También, con los repositorios, conocí una forma de de utilizar metodos de consulta y guardado sin escribir en SQL. |

---

### Prompt original
> "Genera las 7 entidades JPA con estos campos, usando Integer para los ids y el paquete com.bitacora.viajes.model, respetando las restricciones del diagrama y los 7 repositorios Spring Data JPA (JpaRepository) con Integer como id, en el paquete com.bitacora.viajes.repository, sin métodos extra."

### Respuesta de IA
Se devolvieron 7 entidades y 3 enums. Las entidades usan `@Entity`, `@Table`, `@Id` con `IDENTITY`, `@Column`, `@ManyToOne` con `LAZY`, `@UniqueConstraint`, `@OnDelete` y `@Check`. Los enums se guardan como texto. También se devolvieron 7 interfaces que extienden `JpaRepository<Entidad, Integer>`, sin métodos adicionales.

### Diferencias entre versiones
```text
69	1	backend/src/main/java/com/bitacora/viajes/model/Actividad.java
9	1	backend/src/main/java/com/bitacora/viajes/model/CategoriaGasto.java
6	1	backend/src/main/java/com/bitacora/viajes/model/EstadoViaje.java
80	1	backend/src/main/java/com/bitacora/viajes/model/Gasto.java
49	1	backend/src/main/java/com/bitacora/viajes/model/ParticipanteViaje.java
6	1	backend/src/main/java/com/bitacora/viajes/model/RolParticipante.java
47	1	backend/src/main/java/com/bitacora/viajes/model/Tarea.java
40	1	backend/src/main/java/com/bitacora/viajes/model/Usuario.java
58	1	backend/src/main/java/com/bitacora/viajes/model/Viaje.java
43	1	backend/src/main/java/com/bitacora/viajes/model/Voto.java
7	1	backend/src/main/java/com/bitacora/viajes/repository/ActividadRepository.java
7	1	backend/src/main/java/com/bitacora/viajes/repository/GastoRepository.java
7	1	backend/src/main/java/com/bitacora/viajes/repository/ParticipanteViajeRepository.java
7	1	backend/src/main/java/com/bitacora/viajes/repository/TareaRepository.java
8	1	backend/src/main/java/com/bitacora/viajes/repository/UsuarioRepository.java
7	1	backend/src/main/java/com/bitacora/viajes/repository/ViajeRepository.java
7	1	backend/src/main/java/com/bitacora/viajes/repository/VotoRepository.java
```

---
### Pruebas
```
./mvnw compile
[INFO] Scanning for projects...
[INFO] Loaded 24469 auto-discovered prefixes for remote repository central (prefixes-central.txt)
[INFO]
[INFO] --------------------< com.bitacora:bitacora-viajes >--------------------
[INFO] Building bitacora-viajes 0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] Loaded 64 auto-discovered prefixes for remote repository apache.snapshots (prefixes-apache.snapshots.txt)
[INFO]
[INFO] --- resources:3.3.1:resources (default-resources) @ bitacora-viajes ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 9 resources from src/main/resources to target/classes
[INFO]
[INFO] --- compiler:3.11.0:compile (default-compile) @ bitacora-viajes ---
[INFO] Changes detected - recompiling the module! :source
[INFO] Compiling 74 source files with javac [debug release 17] to target/classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  8.664 s
[INFO] Finished at: 2026-11-12T23:09:20-06:00
[INFO] ------------------------------------------------------------------------

mysql -u bitacora_app -p bitacora_viaje
s -e "DESCRIBE participante_viaje; DESCRIBE voto;"
Enter password:
+------------+------------------------------------+------+-----+---------+----------------+
| Field      | Type                               | Null | Key | Default | Extra          |
+------------+------------------------------------+------+-----+---------+----------------+
| id         | int                                | NO   | PRI | NULL    | auto_increment |
| viaje_id   | int                                | NO   | MUL | NULL    |                |
| usuario_id | int                                | NO   | MUL | NULL    |                |
| rol        | enum('ORGANIZADOR','PARTICIPANTE') | NO   |     | NULL    |                |
+------------+------------------------------------+------+-----+---------+----------------+
+--------------+------+------+-----+---------+----------------+
| Field        | Type | Null | Key | Default | Extra          |
+--------------+------+------+-----+---------+----------------+
| id           | int  | NO   | PRI | NULL    | auto_increment |
| actividad_id | int  | NO   | MUL | NULL    |                |
| usuario_id   | int  | NO   | MUL | NULL    |                |
+--------------+------+------+-----+---------+----------------+
```

## Observaciones adicionales

Los ids son `Integer` porque el diagrama usa `INT`. Las restricciones `CHECK` se escribieron con `@Check` y conviene revisarlas con `SHOW CREATE TABLE`. Los repositorios no tienen métodos extra, porque el `DataLoader` solo usa `save` y `count`.

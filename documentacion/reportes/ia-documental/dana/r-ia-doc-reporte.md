# Reporte de Uso Documental en el Proyecto

**Fecha:** 11-11-2026
**Número de reporte:** 01 (Dana Carmona Santamaría)

---

# 1. Breve descripción de uso

Consultas para entender la configuración del proyecto Spring Boot: las dependencias que da Spring Initializr, las clases de entidades JPA, el registro de usuarios y el funcionamiento de Spring Security en el login y la sesión.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 12-11-2026 | Claude | [Consulta 1: Uso de Spring Initializr](#consulta-1-uso-de-spring-initializr) | [Respuesta 1](#respuesta-1) |
| 12-11-2026 | Claude | [Consulta 2: Dependencias que faltan en el pom](#consulta-2-dependencias-que-faltan-en-el-pom) | [Respuesta 2](#respuesta-2) |
| 12-11-2026 | Claude | [Consulta 3: Funcionamiento del registro](#consulta-3-funcionamiento-del-registro) | [Respuesta 3](#respuesta-3) |
| 12-11-2026 | Claude | [Consulta 4: Spring Security y el login](#consulta-4-spring-security-y-el-login) | [Respuesta 4](#respuesta-4) |
| 12-11-2026 | Claude | [Consulta 5: Clases de la carpeta model](#consulta-5-clases-de-la-carpeta-model) | [Respuesta 5](#respuesta-5) |

---

### Consulta 1: Uso de Spring Initializr
> "¿Cómo uso start.spring.io para agregar las dependencias necesarias y qué hace cada una?"

#### Respuesta 1
El formulario pide el tipo de proyecto (Maven), el lenguaje (Java), la versión de Spring Boot, el grupo (`com.bitacora`), el nombre del artefacto (`bitacora-viajes`), el paquete (`com.bitacora.viajes`) y la versión de Java (17). Cada dependencia agrega una parte. Spring Web sirve para crear los controladores REST. Thymeleaf genera las páginas HTML en el servidor. Spring Data JPA conecta las entidades con la base de datos. MySQL Driver permite a Java hablar con MySQL. DevTools reinicia la aplicación al guardar cambios.

---

### Consulta 2: Dependencias que faltan en el pom
> "¿Mi pom.xml tiene todas las dependencias necesarios? Si falta algo, ¿cómo lo agrego?"

#### Respuesta 2
Con solo las cinco dependencias de clase faltan tres. Validation sirve para las anotaciones como `@NotBlank` y `@Email`. Security da el `PasswordEncoder` con BCrypt que usa el `DataLoader`. OpenPDF no aparece en Initializr, así que se agrega a mano con la versión 2.0.3. Se pueden agregar desde Initializr o pegando los bloques dentro de `<dependencies>`. Después hay que compilar con `./mvnw compile`. Al agregar Security, las rutas piden sesión hasta que se configure `SecurityConfig`.

---

### Consulta 3: Funcionamiento del registro
> "¿Qué hace cada parte del registro de usuario (DTO, servicio, controlador y repositorio)?"

#### Respuesta 3
El DTO `RegistroRequest` recibe el JSON y revisa sus campos con anotaciones. El servicio pasa el correo a minúsculas, revisa que no exista, guarda la contraseña con hash y guarda el usuario. El controlador responde 400 si hay errores y 201 si el registro fue correcto. El repositorio tiene el método `existsByCorreo`, que sirve para revisar si un correo ya está usado.

---

### Consulta 4: Spring Security y el login
> "¿Qué hace Spring Security en el login y por qué hay que configurar SecurityConfig?"

#### Respuesta 4
Spring Security decide qué rutas son públicas y cuáles piden sesión. `SecurityConfig` define esas reglas y también qué responder cuando falta la sesión. En el login, el `AuthenticationManager` revisa la contraseña. Si es correcta, la identidad se guarda en la sesión. Las siguientes peticiones llevan la cookie `JSESSIONID`, así que el sistema reconoce al usuario sin pedirle la contraseña otra vez.

---

### Consulta 5: Clases de la carpeta model
> "Explícame el funcionamiento de cada archivo de la carpeta model (entidades y enums)."

#### Respuesta 5
Cada entidad se convierte en una tabla. `@Entity` y `@Table` indican la tabla, y `@Id` con `@GeneratedValue(IDENTITY)` indica la llave primaria que MySQL incrementa sola. `@ManyToOne` con `@JoinColumn` crea las llaves foráneas. `@UniqueConstraint` evita repetidos en `usuario`, `participante_viaje` y `voto`. `@OnDelete(CASCADE)` borra los votos cuando se borra su actividad. `@Check` agrega las reglas del diagrama y `@ColumnDefault` pone valores por defecto. Los enums `RolParticipante`, `EstadoViaje` y `CategoriaGasto` se guardan como texto.

---

## Observaciones adicionales

Estas consultas me ayudaron a elegir las dependencias del `pom.xml` y a entender las clases que cree, como se conecta con la base, como y porque se utiliza cada función.

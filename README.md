# Bitácora de viajes colaborativa

Aplicación web para organizar viajes en grupo: itinerario por días, votación de actividades y control de presupuesto y gastos, todo en un solo lugar.

> **Estado:** README inicial (HU-01 / HU-02). La versión final reproducible la completa Citlaly en HU-41.

## ¿Qué problema resuelve?

La planeación de un viaje en grupo termina dispersa en chats, notas y capturas; las decisiones las toman quienes más hablan y nadie sabe cuánto se ha gastado ni cuánto le toca a cada quien. Esta plataforma centraliza:

- **Itinerario** por días del viaje.
- **Votación** de actividades (la más votada de cada día se calcula automáticamente).
- **Presupuesto** total y por persona, gastos y balance de deudas del grupo.

## Tecnologías y arquitectura (3 capas)

| Capa | Tecnología |
|---|---|
| Presentación | HTML, CSS y JavaScript (Thymeleaf + `fetch()` para actualizar sin recargar) |
| Lógica de negocio | Java 17 + Spring Boot (Web, Validation, Security) |
| Acceso a datos | Spring Data JPA + MySQL |

El árbol de archivos del proyecto está en [`arbol_proyecto.txt`](arbol_proyecto.txt).

## Equipo

| Integrante | Integrante | Integrante | Integrante | Integrante |
|---|---|---|---|---|
| Alfonso | Stephy | Dana | Diego | Citlaly |

## Entorno de desarrollo (WSL)

Todo el equipo trabaja desde **WSL (Ubuntu)** con JDK 17+, Maven, MySQL y Git.
Sigue la guía paso a paso: [`docs/guia-instalacion-wsl.md`](docs/guia-instalacion-wsl.md).

Resumen rápido:

```bash
sudo apt update && sudo apt install -y openjdk-17-jdk maven mysql-server git
sudo service mysql start        # MySQL NO arranca solo en WSL
```

## Configuración local

```bash
cd src/main/resources
cp application.properties.example application.properties
# edita application.properties con tu usuario/contraseña de MySQL
```

`application.properties` **nunca se sube** a GitHub (está en `.gitignore`); solo se versiona el `.example`.

## Ejecución

> Disponible a partir de HU-07 (proyecto Spring Boot).

```bash
git clone git@github.com:Peps06/desarrollo-web.git
cd desarrollo-web
./mvnw spring-boot:run
# Frontend: http://localhost:8080
```

---

## Flujo de trabajo Git (HU-01)

Reglas para los cinco integrantes. **Léelas antes de tu primer commit.**

### Reglas

1. **`main` está protegida:** nadie hace `push` directo a `main`. Todo entra por pull request.
2. **Una rama por historia de usuario**, con el formato `feature/hu-XX-nombre-corto`.
   Ejemplos: `feature/hu-08-registro-backend`, `feature/hu-30-votacion-fetch`.
3. **Mensajes de commit con prefijo `HU-XX:`**, en español y en presente.
   Ejemplos: `HU-08: agrega validación de correo repetido`, `HU-30: deshabilita el botón mientras se vota`.
4. **Todo pull request necesita al menos 1 revisión de otro integrante** (nunca se aprueba el propio) antes de fusionarse a `main`.
5. **Nunca se suben credenciales:** ni `application.properties`, ni contraseñas, ni tokens.
6. Si cambias un contrato de la API (ruta, JSON, códigos), **actualiza `docs/api.md` en la misma rama** y avisa en el grupo.

### Paso a paso para cada historia

```bash
# 1. Partir siempre de un main actualizado
git checkout main
git pull origin main

# 2. Crear tu rama de la historia
git checkout -b feature/hu-XX-nombre-corto

# 3. Trabajar y hacer commits pequeños
git add <archivos>
git commit -m "HU-XX: descripción del cambio"

# 4. Mantener tu rama al día con main (antes de abrir el PR)
git fetch origin
git merge origin/main

# 5. Subir la rama
git push -u origin feature/hu-XX-nombre-corto
```

6. En GitHub abre el **pull request** hacia `main` (la plantilla `.github/pull_request_template.md` te guía) y pide revisión a otro integrante.
7. El revisor comenta o aprueba; con la aprobación se fusiona (**Squash and merge** o **Merge**, según acuerde el equipo) y se borra la rama.
8. Todos hacen `git pull origin main` antes de empezar su siguiente historia.

### Prueba del flujo (cierre de HU-01)

Cada integrante, desde su WSL: clona el repositorio, crea la rama `feature/hu-01-prueba-<nombre>`, hace un commit con prefijo `HU-01:`, abre un pull request y se fusiona a `main`.
Verificación: `git log --oneline` en `main` muestra al menos un commit de cada integrante.

### Definition of Done

Una historia está terminada cuando: se probó con cURL o en el navegador (éxito y error), el commit lleva `HU-XX:`, el pull request fue revisado por otro integrante y está fusionado a `main`. (Versión completa en `docs/definition-of-done.md`, HU-03.)

---

## Pendiente de completar (HU-41)

- [ ] Diagrama de la arquitectura de 3 capas
- [ ] Usuarios y datos de prueba para entrar directo
- [ ] Endpoints REST con ejemplos (resumen de `docs/api.md`)
- [ ] Diagrama del modelo de datos (`docs/diagrama-er.png`)
- [ ] Problemas comunes y cómo correr las pruebas (`./mvnw test`)

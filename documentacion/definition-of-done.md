<!-- HU-03 | Responsable: Stephy | Criterios para considerar terminada una historia de usuario. -->

# Definition of Done — Bitácora de viajes colaborativa

**Responsable:** Stephanie Elizdeth Hernández Prieto
**Proyecto:** Bitácora de viajes colaborativa

Una historia de usuario (HU) se considera terminada solo cuando cumple **todos** los puntos de esta lista. No basta con que el código exista: tiene que estar probado, revisado y en `main`.

## 1. Para cada historia de usuario

- [ ] El criterio de aceptación de la HU se cumple tal como está escrito en el backlog, contra el código y no solo contra el documento.
- [ ] Probado a mano: con `curl` si es backend, o en el navegador si es frontend, con un usuario de prueba y con los datos de prueba del proyecto.
- [ ] La prueba sobrevive a recargar la página y a reiniciar la aplicación: lo que se guarda en MySQL sigue ahí después.
- [ ] Los errores responden como dice `requerimientos.md`: `{codigo, mensaje}`, con 400 para datos inválidos, 401 sin sesión, 403 sin permiso y 404 cuando no existe. Un dato inválido nunca produce un 500.
- [ ] La identidad del usuario se toma siempre de la sesión, nunca de un id que mande el cliente.
- [ ] Si la HU toca dinero, los montos se calculan con 2 decimales y el presupuesto se recalcula después de crear, editar o eliminar.
- [ ] Si la HU toca el frontend, el cambio se ve sin recargar la página cuando así lo pide el criterio (fetch), y los mensajes de éxito o error usan `mensajes.js`.
- [ ] Commit con el formato `HU-XX: descripción corta`.
- [ ] Pull request revisado por otro integrante del equipo y fusionado a `main`. Nada se sube directo a `main`.
- [ ] `git log` muestra commits de los 5 integrantes del equipo.

## 2. Para el código

- [ ] La lógica de negocio vive en los servicios, no en los controladores (por ejemplo, los cálculos de presupuesto y balance van en `PresupuestoService` y `BalanceService`).
- [ ] Las respuestas de la API usan DTOs, no entidades JPA, y las consultas evitan el problema N+1.
- [ ] Las fechas van en formato ISO (`yyyy-MM-dd`) y el JSON en camelCase.
- [ ] Las contraseñas se guardan con hash BCrypt y nunca se devuelven en texto plano.
- [ ] `application.properties` con las credenciales queda fuera del repositorio; solo se sube el `.example` sin credenciales.
- [ ] Los archivos que solo viven en `frontend/` no se duplican dentro del backend: Maven los copia a `static/` al compilar.

## 3. Para la entrega (versión completa)

- [ ] Todas las HU de la fase están terminadas según el punto 1.
- [ ] El flujo completo funciona de principio a fin en el navegador: registro, login, crear viaje, listar, abrir el detalle por días, proponer y votar actividades, registrar gastos y ver el balance.
- [ ] Se probó con una clonación limpia, siguiendo solo el README: `git clone`, `application.properties.example`, usuarios de prueba y `localhost:8080`.
- [ ] El README explica cómo instalar, configurar MySQL y ejecutar el proyecto, con una sección de problemas comunes.
- [ ] La documentación está al día: `requerimientos.md`, `api.md`, `pruebas.md` y los reportes de avance reflejan lo que realmente hay en el código.
- [ ] Se crea el tag de la versión (por ejemplo, `v1.0`) sobre `main`.

## 4. Qué no cuenta como terminado

- Código que solo funciona en la computadora de quien lo escribió.
- Una HU marcada como hecha en el Excel o en el backlog sin su commit y su pull request.
- Una prueba que solo se vio una vez en pantalla sin recargar.
- Un cambio que rompe algo que ya funcionaba y nadie revisó.

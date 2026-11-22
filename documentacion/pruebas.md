# Pruebas del sistema

**Responsable:** Dana Carmona Santamaría
**Fecha:** 22-11-2026

**Objetivo:** como equipo de desarrollo, queremos recorrer los flujos completos probando casos de éxito y de error, para entregar un sistema sin defectos.
---

## 1. Preparación

```
# MySQL y la aplicación (desde backend/)
sudo service mysql restart
./mvnw spring-boot:run

# En otra terminal, sesiones de prueba:
BASE=http://localhost:8080
# Ana (organizadora) y Luis (participante). Contraseña de los dos: Password123
curl -c cookies-ana.txt -H "Content-Type: application/json" -d '{"correo":"ana@example.com","contrasena":"Password123"}' $BASE/api/auth/login
curl -c cookies-luis.txt -H "Content-Type: application/json" -d '{"correo":"luis@example.com","contrasena":"Password123"}' $BASE/api/auth/login

# Id del viaje de prueba 
curl -b cookies-ana.txt $BASE/api/viajes
VIAJE=1
```

**Datos semilla:** 
 Ana López (`ana@example.com`, organizadora) y Luis Pérez (`luis@example.com`, participante), los dos con contraseña `Password123`. Viaje de Oaxaca, Oaxaca, del 20 al 27 de diciembre de 2026, presupuesto 15000 MXN. Actividades: "Visita a Hierve el Agua" (Ana) y "Cena en el Mercado 20 de Noviembre" (Luis).

Al terminar las pruebas, borra las cookies: `rm cookies-ana.txt cookies-luis.txt cookies-nueva.txt` (no están en el `.gitignore`).

---

## 2. Pruebas unitarias (`./mvnw test`)

```
cd backend
./mvnw test

# Resesultados
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.518 s -- in com.bitacora.viajes.BitacoraViajesApplicationTests
[INFO] Running com.bitacora.viajes.service.ActividadGanadoraServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.bitacora.viajes.service.ActividadGanadoraServiceTest
[INFO] Running com.bitacora.viajes.service.BalanceServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.bitacora.viajes.service.BalanceServiceTest
[INFO] Running com.bitacora.viajes.service.PresupuestoServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.bitacora.viajes.service.PresupuestoServiceTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  13.520 s
[INFO] Finished at: 2026-11-22T16:45:16-06:00
[INFO] ------------------------------------------------------------------------

```
---

## 3. Flujo completo en el navegador

Recorrer en orden, con la sesión de Ana (organizadora).

| # | Paso | Resultado |
| :--- | :--- | :--- | 
| 1 | Registrar una usuaria nueva en `/registro` (por ejemplo `prueba@ejemplo.com`, `Password123`) | 201, mensaje de éxito, redirige a `/login` |
| 2 | Iniciar sesión como Ana en `/login` | Entra a `/viajes` y ve el viaje de Oaxaca |
| 3 | Crear viaje en `/viajes/nuevo` (destino, fechas, presupuesto, moneda) | Aparece en la lista y en su detalle; Ana queda como organizadora |
| 4 | Agregar participantes (agregar a la usuaria nueva por correo) | Aparece en la lista de participantes |
| 5 | Proponer actividades (título, día, hora, costo) | Aparece en el itinerario del día |
| 6 | Votar y quitar voto | El contador sube a 1 y baja a 0 sin recargar la página |
| 7 | Registrar gastos: uno en MXN y uno en USD con comprobante | Aparecen en la lista; en el de USD se ve el equivalente en MXN antes de enviar y el gasto queda guardado ya convertido; el comprobante se puede ver |
| 8 | Ver presupuesto y balance | La barra muestra gastado y restante; las tarjetas de saldo (verde si le deben, rojo si debe, neutra si está en 0) y las transferencias se ven bien |
| 9 | Checklist: agregar una tarea, marcarla y borrarla | El contador "X de Y completadas" cambia y el tachado se guarda |
| 10 | Exportar PDF (botón "Exportar PDF") | Descarga el archivo `itinerario-Oaxaca.pdf` (o con el destino del viaje) |
| 11 | Editar y eliminar viaje | Se puede editar los datos y cancelar el viaje |
| 12 | Cerrar sesión (botón en la barra) | Redirige a `/login`; al entrar a `/viajes` sin sesión pide iniciar sesión |

---

## 4. Casos de error por endpoint

Un caso por cada tipo de error, sin repetir, con cURL o en el navegador. Variables: `$BASE`, `$VIAJE`, `cookies-ana.txt` (Ana, organizadora), `cookies-luis.txt` (Luis, participante) y `cookies-nueva.txt` (la usuaria del paso 1 del flujo, que no participa en el viaje). Un `401` se prueba con cURL sin `-b` (sin sesión).

| # | Endpoint | Caso de error | Comando | Resultado |
| :--- | :--- | :--- | :--- | :--- |
| 1 | `POST /api/auth/registro` | Correo ya registrado(Sesión Ana) | `curl -H "Content-Type: application/json" -d '{"nombre":"Ana","correo":"ana@example.com","contrasena":"Password123"}' $BASE/api/auth/registro` |{"codigo":400,"mensaje":"El correo ya está registrado"}|
| 2 | `POST /api/auth/login` | Cuerpo vacío | `curl -H "Content-Type: application/json" -d '{}' $BASE/api/auth/login` | "errores":[{"campo":"correo","mensaje":"El correo es obligatorio"}]|
| 3 | `POST /api/auth/logout` | Sin sesión | `curl -i -X POST $BASE/api/auth/logout` | 204 |
| 4 | `POST /api/viajes` | Presupuesto en 0 | `curl -b cookies-ana.txt -H "Content-Type: application/json" -d '{"destino":"CDMX","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":0,"monedaBase":"MXN"}' $BASE/api/viajes` | "errores":{"codigo":400,"mensaje":"Datos inválidos","errores":[{"campo":"presupuestoTotal","mensaje":"El presupuesto debe ser mayor a cero"}]} |
| 5 | `GET /api/viajes` | Sin sesión | `curl -i $BASE/api/viajes` | {"codigo":401,"mensaje":"Tu sesión expiró"} |
| 6 | `GET /api/viajes/{id}` | Viaje inexistente | `curl -b cookies-ana.txt $BASE/api/viajes/9999` | {"codigo":404,"mensaje":"El viaje no existe"} |
| 7 | `PUT /api/viajes/{id}` | Sesión Luis (no organizador) edita | `curl -b cookies-luis.txt -X PUT -H "Content-Type: application/json" -d '{"destino":"CDMX","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":1000,"monedaBase":"MXN"}' $BASE/api/viajes/$VIAJE` | {"codigo":403,"mensaje":"Solo el organizador puede editar o cancelar el viaje"} |
| 8 | `GET /api/viajes/{id}/participantes` | Sesión usuaria que no participa | `curl -b cookies-nueva.txt $BASE/api/viajes/$VIAJE/participantes` | {"codigo":403,"mensaje":"No participas en este viaje"}d |
| 9 | `POST /api/viajes/{id}/participantes` | Correo sin cuenta | `curl -b cookies-ana.txt -X POST -H "Content-Type: application/json" -d '{"correo":"nadie@ejemplo.com"}' $BASE/api/viajes/$VIAJE/participantes` | {"codigo":404,"mensaje":"No existe una cuenta registrada con ese correo"} |
| 10 | `POST /api/viajes/{id}/actividades` | Título vacío | `curl -b cookies-ana.txt -H "Content-Type: application/json" -d '{"titulo":"","descripcion":"x","dia":"2026-12-21","hora":"09:00","costoEstimado":100}' $BASE/api/viajes/$VIAJE/actividades` | {"codigo":400,"mensaje":"Datos inválidos","errores":[{"campo":"titulo","mensaje":"El título es obligatorio"}]} |
| 11 | `POST /api/actividades/{id}/votos` | Voto duplicado (votar dos veces seguidas) | `curl -b cookies-ana.txt -X POST $BASE/api/actividades/1/votos` (dos veces) | {"codigo":400,"mensaje":"Ya votaste por esta actividad"} |
| 12 | `DELETE /api/actividades/{id}/votos` | Quitar voto sin haber votado | `curl -b cookies-luis.txt -X DELETE $BASE/api/actividades/1/votos` | {"codigo":404,"mensaje":"No has votado por esta actividad"} |
| 13 | `POST /api/gastos` | Moneda original inválida ("US") | `curl -b cookies-ana.txt -H "Content-Type: application/json" -d '{"viajeId":1,"concepto":"Taxi","categoria":"transporte","monto":100,"monedaOriginal":"US","montoOriginal":100,"tipoCambio":1}' $BASE/api/gastos` | {"codigo":400,"mensaje":"El cuerpo de la petición no es un JSON válido"} |
| 14 | `GET /api/gastos/{id}/comprobante` | Gasto sin comprobante | `curl -b cookies-ana.txt $BASE/api/gastos/1/comprobante` | {"codigo":404,"mensaje":"Este gasto no tiene comprobante"} | |
| 15 | `POST /api/viajes/{id}/tareas` | Descripción vacía | `curl -b cookies-ana.txt -H "Content-Type: application/json" -d '{"descripcion":""}' $BASE/api/viajes/$VIAJE/tareas` | {"codigo":400,"mensaje":"Datos inválidos","errores":[{"campo":"descripcion","mensaje":"La descripción de la tarea es obligatoria"}]} |
| 16 | `PATCH /api/tareas/{id}/toggle` | Tarea inexistente | `curl -b cookies-ana.txt -X PATCH $BASE/api/tareas/9999/toggle` | {"codigo":404,"mensaje":"La tarea no existe"} |

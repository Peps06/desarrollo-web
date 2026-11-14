# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 13/11/2026
**Número de reporte:** 01

---

# 1. Breve descripción de uso

Se usó Mistral Vibe en Visual Studio Code para revisar el módulo de gastos (entidad, repositorio, servicio, controlador, DTOs y manejo de excepciones) y detectar errores, sus causas y propuestas de solución. El API de gastos se desarrolló antes sin IA (commit e72d585); después de la revisión se aplicaron correcciones (commit d8fd7f0).

---

# 2. Evidencias de uso

| Evidencia                   | Formato                                                                   |
| --------------------------- | ------------------------------------------------------------------------- |
| Herramienta                 | Mistral Vibe (extensión en Visual Studio Code)                            |
| Modelo/temperatura          | Mistral Medium 3.5 / no configurada (valor por defecto de la herramienta) |
| Prompt original             | Ver subsección 2.1                                                        |
| Respuesta de IA             | Ver subsección 2.2                                                        |
| Código anterior             | Commit (e72d585)                                                          |
| Código posterior            | Commit (d8fd7f0)                                                          |
| Diferencias entre versiones | Ver subsección 2.3                                                        |
| Prueba de funcionamiento    | Ver subsección 2.4                                                        |
| Código de uso               | Revisión                                                                  |
| Reflexión del estudiante    | Ver subsección 2.5                                                        |

## 2.1 Prompt original

<details>
<summary>Ver prompt completo</summary>

```text
Actúa como un revisor de código senior. Haz una revisión completa del módulo de GASTOS y sus complementos en este proyecto (Bitácora de viajes colaborativa: Java + Spring Boot, Hibernate/JPA, MySQL, frontend HTML/CSS/JS con Fetch).

ALCANCE
- Backend: entidad, repositorio, servicio y controlador REST de gastos (POST /api/gastos, GET /api/viajes/{id}/presupuesto).
- Base de datos: tabla gasto, sus relaciones (viaje, usuario, participante_viaje), tipos de dato y restricciones.
- Frontend: HTML/JS que consume esos endpoints, validaciones y manejo de errores.
- Complementos relacionados con gastos, si existen: presupuesto compartido, balance de deudas, comprobantes y conversor de divisas.

QUÉ VERIFICAR
1. Flujo completo: formulario → Fetch → controlador → servicio → BD → respuesta → pantalla.
2. Cálculos: totales, presupuesto, balance entre participantes, redondeos y divisas.
3. Validaciones: montos nulos o negativos, viaje inexistente, usuario que no participa en el viaje, campos vacíos.
4. Consistencia entre capas: nombres y tipos de campos entre JSON, DTO, entidad y columna.
5. Permisos: que solo participantes del viaje puedan ver o registrar gastos.
6. Código duplicado, código muerto y manejo de excepciones.

REGLAS
- Solo análisis: no modifiques ni crees archivos.
- Si algo no se puede comprobar (no hay pruebas, no se puede ejecutar), dilo explícitamente en lugar de suponerlo.
- Cita siempre archivo y línea.

BITÁCORA DEL ANÁLISIS (formato de la respuesta)
1. Archivos revisados y qué se comprobó de cada uno.
2. Por cada hallazgo:
   - ID y severidad (crítico, alto, medio, bajo)
   - Ubicación (archivo:línea)
   - Qué ocurre y cómo reproducirlo
   - Por qué ocurre (causa raíz)
   - Propuesta de solución, con un fragmento de código sugerido sin aplicarlo
3. Lo que sí funciona correctamente.
4. Resumen final: tabla de hallazgos y orden recomendado de corrección.
```

</details>

## 2.2 Respuesta de IA

<details>
<summary>Ver bitácora completa de análisis de Vibe</summary>

# **BITÁCORA DEL ANÁLISIS - MÓDULO DE GASTOS**

---

## **1. ARCHIVOS REVISADOS Y QUÉ SE COMPROBÓ DE CADA UNO**

### **Backend - Entidad y Repositorio**
| Archivo | Líneas | Qué se comprobó |
|---------|--------|-----------------|
| `Gasto.java` | 151 | Estructura de entidad JPA, columnas, relaciones (@ManyToOne con Viaje y Usuario), restricciones (@Check monto > 0), tipos de dato (BigDecimal para montos), campos de divisas (monedaOriginal, montoOriginal, tipoCambio), comprobante (imagenUrl) |
| `GastoRepository.java` | 29 | Métodos: `sumarMontoPorViaje()` (query para total), `esRolEnViaje()` (validación de permisos) |
| `CategoriaGasto.java` | 9 | Enum con categorías: COMIDA, TRANSPORTE, HOSPEDAJE, ACTIVIDADES, OTROS |

### **Backend - Servicio y Controlador**
| Archivo | Líneas | Qué se comprobó |
|---------|--------|-----------------|
| `GastoService.java` | 108 | Lógica: actualizar, eliminar, validaciones (permiso, datos), manejo de comprobantes, transaccionalidad |
| `GastoController.java` | 41 | Endpoints: PUT /api/gastos/{id}, DELETE /api/gastos/{id}, uso de Authentication |
| `AlmacenamientoService.java` | 27 | Eliminación de comprobantes del disco |

### **Backend - DTOs**
| Archivo | Líneas | Qué se comprobó |
|---------|--------|-----------------|
| `GastoRequest.java` | 26 | Campos: concepto, categoria, monto (sin viajeId ni usuarioId) |
| `GastoResponse.java` | 56 | Campos: id, concepto, categoria, monto, fecha, usuarioId |
| `GastoRegistradoResponse.java` | 28 | Incluye gasto + totalViaje |

### **Backend - Relacionados (Viajes y Participantes)**
| Archivo | Líneas | Qué se comprobó |
|---------|--------|-----------------|
| `Viaje.java` | 58 | Campos: presupuestoTotal, monedaBase (default MXN) |
| `ParticipanteViaje.java` | 49 | Relación Viaje-Usuario con rol (ORGANIZADOR/PARTICIPANTE) |
| `ViajeDetalleService.java` | 127 | Incluye `sumarMontoPorViaje()` en el detalle, validación de participación |
| `ViajeDetalleController.java` | 36 | GET /api/viajes/{id} devuelve totalGastado |
| `ParticipanteViajeRepository.java` | 40 | Método `buscarParticipacion(viajeId, correo)` para validar acceso |

### **Backend - Excepciones**
| Archivo | Líneas | Qué se comprobó |
|---------|--------|-----------------|
| `GlobalExceptionHandler.java` | 35 | Manejo de RecursoNoEncontradoException, AccesoDenegadoException, DatosInvalidosException |
| `AccesoDenegadoException.java` | 14 | Extiende RuntimeException (403) |
| `DatosInvalidosException.java` | 14 | Extiende RuntimeException (400) |
| `RecursoNoEncontradoException.java` | - | Extiende RuntimeException (404) |

### **Backend - NO IMPLEMENTADOS (solo comentarios)**
| Archivo | Estado |
|---------|--------|
| `PresupuestoController.java` | **VACÍO** (solo comentario) |
| `PresupuestoService.java` | **VACÍO** (solo comentario) |
| `PresupuestoResponse.java` | **VACÍO** (solo comentario) |
| `BalanceController.java` | **VACÍO** (solo comentario) |
| `BalanceService.java` | **VACÍO** (solo comentario) |
| `BalanceResponse.java` | **VACÍO** (solo comentario) |
| `ParticipanteService.java` | **VACÍO** (solo comentario) |

### **Frontend - NO IMPLEMENTADOS**
| Archivo | Estado |
|---------|--------|
| `frontend/js/gastos.js` | **VACÍO** (solo comentario) |
| `frontend/js/presupuesto.js` | **VACÍO** (solo comentario) |
| `frontend/js/balance.js` | **VACÍO** (solo comentario) |
| `frontend/js/comprobantes.js` | **VACÍO** (solo comentario) |
| `frontend/js/divisas.js` | **VACÍO** (solo comentario) |
| `templates/viajes/detalle.html` | **VACÍO** (solo comentario) |
| `templates/viajes/lista.html` | **VACÍO** (solo comentario) |
| `templates/viajes/nuevo.html` | **VACÍO** (solo comentario) |

---

---

## **2. HALLAZGOS**

---

### **🔴 CRÍTICOS**

#### **CR-001: Endpoint POST /api/gastos NO EXISTE**
- **Severidad**: Crítico
- **Ubicación**: Falta en `GastoController.java`
- **Qué ocurre**: El alcance especifica POST /api/gastos para registrar gastos, pero solo existen PUT /api/gastos/{id} y DELETE /api/gastos/{id}
- **Causa raíz**: Implementación incompleta del módulo de gastos. Falta el endpoint de creación.
- **Cómo reproducir**: Intentar registrar un gasto con POST /api/gastos → 404 Not Found
- **Solución sugerida**:
  ```java
  @PostMapping
  public ResponseEntity<GastoRegistradoResponse> registrar(
          @RequestBody GastoRequest request,
          @RequestParam Integer viajeId,
          Authentication auth) {
      // Validar que el usuario participa en el viaje
      // Validar datos del request
      // Crear Gasto con viajeId y usuario de la sesión
      // Guardar y devolver GastoRegistradoResponse
  }
  ```

#### **CR-002: Endpoint GET /api/viajes/{id}/presupuesto NO EXISTE**
- **Severidad**: Crítico
- **Ubicación**: Falta en `PresupuestoController.java` (archivo vacío)
- **Qué ocurre**: El alcance especifica este endpoint, pero PresupuestoController.java solo tiene un comentario
- **Causa raíz**: Módulo de presupuesto no implementado
- **Cómo reproducir**: GET /api/viajes/1/presupuesto → 404 Not Found
- **Solución sugerida**:
  ```java
  @RestController
  @RequestMapping("/api/viajes/{id}/presupuesto")
  public class PresupuestoController {
      @Autowired private PresupuestoService service;
      
      @GetMapping
      public ResponseEntity<PresupuestoResponse> obtener(@PathVariable Integer id, Authentication auth) {
          return ResponseEntity.ok(service.obtenerPresupuesto(id, auth.getName()));
      }
  }
  ```

#### **CR-003: Frontend COMPLETAMENTE FALTANTE**
- **Severidad**: Crítico
- **Ubicación**: `frontend/js/*.js`, `templates/*.html`
- **Qué ocurre**: Todos los archivos JS y HTML están vacíos (solo comentarios). No hay implementación de formulario, fetch, manejo de errores ni visualización de datos
- **Causa raíz**: Frontend no desarrollado
- **Impacto**: El usuario no puede interactuar con el módulo de gastos de ninguna manera
- **Solución**: Implementar:
  - Formulario de registro de gastos (concepto, categoría, monto, comprobante)
  - Llamadas fetch a POST /api/gastos y PUT /api/gastos/{id}
  - Consumo de GET /api/viajes/{id} para mostrar totalGastado
  - Validaciones cliente y manejo de errores

---

### **🟠 ALTOS**

#### **AL-001: GastoRequest NO INCLUYE viajeId**
- **Severidad**: Alto
- **Ubicación**: `GastoRequest.java:15-20`
- **Qué ocurre**: El DTO GastoRequest no tiene campo para especificar a qué viaje pertenece el gasto
- **Causa raíz**: Diseño incompleto. El viajeId debe venir del cliente o de la URL
- **Cómo reproducir**: Al intentar registrar un gasto, no hay forma de asociarlo a un viaje
- **Solución sugerida**:
  ```java
  // Opción 1: Añadir a GastoRequest
  public class GastoRequest {
      private Integer viajeId;  // <-- Añadir
      private String concepto;
      private CategoriaGasto categoria;
      private BigDecimal monto;
      // getters/setters
  }
  
  // Opción 2: Usar @RequestParam o path variable
  @PostMapping("/viajes/{viajeId}/gastos")
  public ResponseEntity<...> registrar(@PathVariable Integer viajeId, @RequestBody GastoRequest request, ...)
  ```

#### **AL-002: GastoRequest NO INCLUYE campos de divisas**
- **Severidad**: Alto
- **Ubicación**: `GastoRequest.java:15-20`
- **Qué ocurre**: La entidad `Gasto` tiene campos para manejo de divisas (`monedaOriginal`, `montoOriginal`, `tipoCambio`), pero GastoRequest no los incluye
- **Causa raíz**: Inconsistencia entre DTO y entidad
- **Cómo reproducir**: No se puede registrar un gasto en otra divisa
- **Solución sugerida**:
  ```java
  public class GastoRequest {
      private String concepto;
      private CategoriaGasto categoria;
      private BigDecimal monto;
      private String monedaOriginal;  // <-- Añadir
      private BigDecimal montoOriginal; // <-- Añadir
      private BigDecimal tipoCambio;   // <-- Añadir
      // getters/setters
  }
  ```

#### **AL-003: Gasto NO VALIDA QUE EL USUARIO PARTICIPE EN EL VIAJE al registrar**
- **Severidad**: Alto
- **Ubicación**: Falta en `GastoService.java` (solo existe validarPermiso para actualizar/eliminar)
- **Qué ocurre**: GastoService.validaPermiso() solo existe para actualizar/eliminar. No hay validación al registrar un gasto que el usuario sea participante del viaje
- **Causa raíz**: Lógica de registro incompleta
- **Cómo reproducir**: Usuario puede registrar gastos en viajes donde no participa (si se implementara POST)
- **Solución sugerida**:
  ```java
  @Transactional
  public GastoRegistradoResponse registrar(GastoRequest request, Integer viajeId, String email) {
      Usuario usuario = buscarUsuario(email);
      // Validar que el usuario participa en el viaje
      boolean participa = gastoRepository.esRolEnViaje(viajeId, usuario.getId(), RolParticipante.ORGANIZADOR)
              || gastoRepository.esRolEnViaje(viajeId, usuario.getId(), RolParticipante.PARTICIPANTE);
      if (!participa) {
          throw new AccesoDenegadoException("No participas en este viaje");
      }
      // ... resto de la lógica
  }
  ```

#### **AL-004: ParticipanteViajeRepository NO TIENE MÉTODO para validar participación simple**
- **Severidad**: Alto
- **Ubicación**: `ParticipanteViajeRepository.java`
- **Qué ocurre**: Para validar si un usuario participa en un viaje, se usa `GastoRepository.esRolEnViaje()` que es específico para roles. No hay método genérico para validar participación
- **Causa raíz**: Diseño acoplado. La validación de participación debería estar en ParticipanteViajeRepository
- **Solución sugerida**:
  ```java
  // En ParticipanteViajeRepository
  @Query("select count(p) > 0 from ParticipanteViaje p where p.viaje.id = :viajeId and p.usuario.id = :usuarioId")
  boolean existeParticipacion(@Param("viajeId") Integer viajeId, @Param("usuarioId") Integer usuarioId);
  ```

#### **AL-005: Gasto NO TIENE VALIDACIÓN DE VIAJE EXISTENTE**
- **Severidad**: Alto
- **Ubicación**: `GastoService.java`
- **Qué ocurre**: Al registrar/actualizar un gasto, no se valida que el viajeId referenciado exista y esté activo
- **Causa raíz**: Validación incompleta
- **Solución sugerida**:
  ```java
  private Viaje buscarViaje(Integer viajeId) {
      return viajeRepository.findById(viajeId)
              .orElseThrow(() -> new RecursoNoEncontradoException("Viaje no encontrado"));
  }
  ```

#### **AL-006: GlobalExceptionHandler usa Map en lugar de ErrorResponse**
- **Severidad**: Alto
- **Ubicación**: `GlobalExceptionHandler.java:20-33`
- **Qué ocurre**: Los manejadores devuelven `Map.of("error", mensaje)` en lugar de usar el DTO `ErrorResponse` que ya existe
- **Causa raíz**: Inconsistencia con el patrón de errores del proyecto
- **Solución sugerida**:
  ```java
  @ExceptionHandler(RecursoNoEncontradoException.class)
  public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new ErrorResponse("recurso", e.getMessage()));
  }
  ```

---

### **🟡 MEDIOS**

#### **ME-001: GastoService NO VALIDA ESTADO DEL VIAJE**
- **Severidad**: Medio
- **Ubicación**: `GastoService.java:45-65`
- **Qué ocurre**: Se pueden registrar/actualizar gastos en viajes CANCELADOS
- **Causa raíz**: No se valida el estado del viaje
- **Solución sugerida**:
  ```java
  private void validarViajeActivo(Viaje viaje) {
      if (viaje.getEstado() == EstadoViaje.CANCELADO) {
          throw new DatosInvalidosException("No se pueden modificar gastos de un viaje cancelado");
      }
  }
  ```

#### **ME-002: GastoResponse NO INCLUYE viajeId**
- **Severidad**: Medio
- **Ubicación**: `GastoResponse.java:15-25`
- **Qué ocurre**: El DTO de respuesta no incluye el id del viaje al que pertenece el gasto
- **Causa raíz**: Inconsistencia con necesidades del frontend
- **Solución sugerida**:
  ```java
  public class GastoResponse {
      private Integer id;
      private Integer viajeId;  // <-- Añadir
      private String concepto;
      // ... resto
      
      public GastoResponse(Gasto gasto) {
          this.id = gasto.getId();
          this.viajeId = gasto.getViaje().getId();  // <-- Añadir
          // ...
      }
  }
  ```

#### **ME-003: Gasto NO TIENE VALIDACIÓN DE MONEDA ORIGINAL**
- **Severidad**: Medio
- **Ubicación**: `GastoService.java:85-95` (validarDatos)
- **Qué ocurre**: No se valida que `monedaOriginal` sea un código ISO válido (3 letras)
- **Causa raíz**: Validación incompleta
- **Solución sugerida**:
  ```java
  if (request.getMonedaOriginal() != null && !request.getMonedaOriginal().matches("^[A-Z]{3}$")) {
      throw new DatosInvalidosException("La moneda debe ser un código de 3 letras (ej. MXN, USD)");
  }
  ```

#### **ME-004: Gasto NO MANEJA ERRORES DE ALMACENAMIENTO**
- **Severidad**: Medio
- **Ubicación**: `GastoService.java:70-75` (eliminar)
- **Qué ocurre**: Si `almacenamientoService.eliminar()` falla, la transacción ya hizo delete en BD pero no en disco
- **Causa raíz**: No hay rollback coordinado
- **Solución sugerida**:
  ```java
  @Transactional
  public void eliminar(Integer id, String email) {
      Gasto gasto = buscarGasto(id);
      validarPermiso(gasto, buscarUsuario(email));
      
      String comprobante = gasto.getImagenUrl();
      gastoRepository.delete(gasto);
      
      if (comprobante != null && !comprobante.isBlank()) {
          try {
              almacenamientoService.eliminar(comprobante);
          } catch (RuntimeException e) {
              // Loggear error pero no fallar la transacción
              // O usar @Transactional(rollbackFor = Exception.class)
          }
      }
  }
  ```

#### **ME-005: DTOs INCOMPLETOS para presupuesto y balance**
- **Severidad**: Medio
- **Ubicación**: `PresupuestoResponse.java`, `BalanceResponse.java`
- **Qué ocurre**: Archivos vacíos. No hay estructura definida para estas respuestas
- **Solución sugerida para PresupuestoResponse**:
  ```java
  public class PresupuestoResponse {
      private BigDecimal presupuestoTotal;
      private BigDecimal totalGastado;
      private BigDecimal disponible;
      private String monedaBase;
      // getters, constructor
  }
  ```

#### **ME-006: Cálculo de tipo de cambio NO VALIDADO**
- **Severidad**: Medio
- **Ubicación**: `Gasto.java:50` (tipoCambio default = 1)
- **Qué ocurre**: `tipoCambio` puede ser nulo o negativo. No hay validación
- **Solución sugerida**:
  ```java
  @Check(constraints = "tipo_cambio > 0")
  // Y en validarDatos:
  if (request.getTipoCambio() != null && request.getTipoCambio().compareTo(BigDecimal.ZERO) <= 0) {
      throw new DatosInvalidosException("El tipo de cambio debe ser mayor a cero");
  }
  ```

---

### **🟢 BAJOS**

#### **BA-001: GastoRequest NO VALIDA LONGITUD DE CONCEPTO**
- **Severidad**: Bajo
- **Ubicación**: `GastoService.java:92` (validar en servicio, no en DTO)
- **Qué ocurre**: La validación de longitud de concepto (≤150) está en el servicio, no en el DTO con anotaciones
- **Solución sugerida**: Usar `@Size(max = 150)` en GastoRequest.concepto

#### **BA-002: Gasto NO TIENE CAMPO para fecha personalizada**
- **Severidad**: Bajo
- **Ubicación**: `Gasto.java:58` (fecha default = now)
- **Qué ocurre**: El campo `fecha` siempre se setea a `LocalDateTime.now()`. No hay forma de registrar un gasto con fecha histórica
- **Solución sugerida**: Permitir fecha en GastoRequest:
  ```java
  public class GastoRequest {
      private LocalDateTime fecha;  // <-- Opcional
      // ...
  }
  ```

#### **BA-003: GastoService NO USA @Transactional(readOnly) para consultas**
- **Severidad**: Bajo
- **Ubicación**: `GastoService.java:67-70` (buscarGasto, buscarUsuario)
- **Qué ocurre**: Métodos de búsqueda no están marcados como readOnly
- **Solución sugerida**:
  ```java
  @Transactional(readOnly = true)
  private Gasto buscarGasto(Integer id) { ... }
  ```

#### **BA-004: GastoRepository NO TIENE MÉTODO para listar gastos por viaje**
- **Severidad**: Bajo
- **Ubicación**: `GastoRepository.java`
- **Qué ocurre**: Falta método para obtener todos los gastos de un viaje (necesario para frontend)
- **Solución sugerida**:
  ```java
  List<Gasto> findByViajeId(Integer viajeId);
  ```

#### **BA-005: NOMBRES INCONSISTENTES entre capas**
- **Severidad**: Bajo
- **Ubicación**: Multiple
- **Qué ocurre**:
  - `GastoService.actualizar()` devuelve `GastoRegistradoResponse` pero debería ser `GastoActualizadoResponse` para consistencia semántica
  - El endpoint para presupuesto no sigue patrón REST (debería ser GET /api/viajes/{id}/presupuesto en lugar de POST)
- **Solución**: Renombrar para consistencia

---

---

## **3. LO QUE SÍ FUNCIONA CORRECTAMENTE**

✅ **Entidad Gasto bien diseñada**:
- Relaciones JPA correctas con Viaje y Usuario (@ManyToOne, LAZY)
- Tipos de dato apropiados (BigDecimal para montos, LocalDateTime para fecha)
- Restricción @Check para monto > 0
- Campos para manejo de divisas (monedaOriginal, montoOriginal, tipoCambio)
- Campo para comprobante (imagenUrl)

✅ **GastoRepository funcional**:
- `sumarMontoPorViaje()` calcula correctamente el total
- `esRolEnViaje()` valida permisos de organizador

✅ **GastoService con lógica de negocio sólida**:
- Validación de permisos (solo autor u organizador puede modificar/eliminar)
- Validación de datos (monto > 0, concepto obligatorio, longitud, categoría)
- Manejo transaccional correcto
- Eliminación de comprobante al borrar gasto

✅ **GastoController bien estructurado**:
- Endpoints REST con convenciones correctas
- Uso adecuado de Authentication
- Códigos HTTP apropiados (200, 204)

✅ **DTOs bien mapeados**:
- GastoResponse construye correctamente desde Gasto
- GastoRegistradoResponse incluye gasto + totalViaje

✅ **Integración con ViajeDetalleService**:
- El endpoint GET /api/viajes/{id} devuelve `totalGastado` calculado desde GastoRepository
- Validación de que el usuario participa en el viaje antes de mostrar detalle

✅ **Manejo de excepciones centralizado**:
- GlobalExceptionHandler captura excepciones de negocio
- Uso de excepciones personalizadas (AccesoDenegadoException, DatosInvalidosException, RecursoNoEncontradoException)

✅ **Base de datos bien modelada**:
- Tabla gasto con restricciones NOT NULL apropiadas
- Relaciones con participante_viaje (a través de Viaje y Usuario)
- Tipos de dato correctos en MySQL

✅ **ParticipanteViaje con restricción única**:
- `@UniqueConstraint` evita duplicados de usuario en mismo viaje

---

---

## **4. RESUMEN FINAL**

### **Tabla de Hallazgos**

| ID | Severidad | archivO:línea | Descripción | Prioridad Corrección |
|-----|------------|---------------|-------------|---------------------|
| **CR-001** | 🔴 Crítico | Falta | Endpoint POST /api/gastos NO EXISTE | **1** |
| **CR-002** | 🔴 Crítico | PresupuestoController.java:1 | Endpoint GET /api/viajes/{id}/presupuesto NO EXISTE | **1** |
| **CR-003** | 🔴 Crítico | frontend/js/*.js | Frontend COMPLETAMENTE FALTANTE | **1** |
| **AL-001** | 🟠 Alto | GastoRequest.java:15-20 | GastoRequest NO INCLUYE viajeId | **2** |
| **AL-002** | 🟠 Alto | GastoRequest.java:15-20 | GastoRequest NO INCLUYE campos de divisas | **2** |
| **AL-003** | 🟠 Alto | GastoService.java | NO VALIDA participación en viaje al registrar | **2** |
| **AL-004** | 🟠 Alto | ParticipanteViajeRepository.java | NO TIENE método para validar participación | **2** |
| **AL-005** | 🟠 Alto | GastoService.java | NO VALIDA existencia de viaje | **2** |
| **AL-006** | 🟠 Alto | GlobalExceptionHandler.java:20-33 | Usa Map en lugar de ErrorResponse | **3** |
| **ME-001** | 🟡 Medio | GastoService.java | NO VALIDA estado ACTIVO del viaje | **4** |
| **ME-002** | 🟡 Medio | GastoResponse.java:15-25 | NO INCLUYE viajeId | **4** |
| **ME-003** | 🟡 Medio | GastoService.java:85-95 | NO VALIDA monedaOriginal | **4** |
| **ME-004** | 🟡 Medio | GastoService.java:70-75 | NO MANEJA errores de almacenamiento | **4** |
| **ME-005** | 🟡 Medio | PresupuestoResponse.java | DTO vacío/incompleto | **3** |
| **ME-006** | 🟡 Medio | Gasto.java:50 | tipoCambio NO VALIDADO | **4** |

---

### **📌 ORDEN RECOMENDADO DE CORRECCIÓN**

#### **Fase 1: Críticos (Bloquean funcionalidad básica)**
1. **CR-001**: Implementar POST /api/gastos en GastoController
2. **CR-002**: Implementar GET /api/viajes/{id}/presupuesto en PresupuestoController
3. **CR-003**: Implementar frontend (gastos.js, HTML templates)

#### **Fase 2: Altos (Errores de diseño y seguridad)**
4. **AL-001**: Añadir viajeId a GastoRequest
5. **AL-002**: Añadir campos de divisas a GastoRequest
6. **AL-003**: Validar participación en viaje al registrar gasto
7. **AL-004**: Añadir método existeParticipacion a ParticipanteViajeRepository
8. **AL-005**: Validar existencia de viaje en GastoService
9. **AL-006**: Usar ErrorResponse en GlobalExceptionHandler

#### **Fase 3: Medios (Mejoras de robustez)**
10. **ME-001**: Validar estado ACTIVO del viaje
11. **ME-002**: Añadir viajeId a GastoResponse
12. **ME-003**: Validar monedaOriginal en GastoService
13. **ME-004**: Manejar errores de almacenamiento
14. **ME-005**: Implementar PresupuestoResponse y BalanceResponse
15. **ME-006**: Validar tipoCambio > 0

#### **Fase 4: Bajos (Mejoras menores)**
16. **BA-001**: Usar anotaciones de validación en GastoRequest
17. **BA-002**: Permitir fecha personalizada en Gasto
18. **BA-003**: Marcar métodos de búsqueda como @Transactional(readOnly)
19. **BA-004**: Añadir método findByViajeId a GastoRepository
20. **BA-005**: Renombrar DTOs para consistencia semántica

---
---
---

## **📊 ESTADO GENERAL DEL MÓDULO**

| Componente | Estado | Completitud | Calidad |
|------------|--------|-------------|---------|
| **Entidad Gasto** | ✅ Implementado | 100% | ⭐⭐⭐⭐ |
| **GastoRepository** | ✅ Implementado | 80% | ⭐⭐⭐⭐ |
| **GastoService** | ✅ Implementado | 70% | ⭐⭐⭐ |
| **GastoController** | ⚠️ Parcial | 50% | ⭐⭐⭐ |
| **DTOs Gastos** | ⚠️ Parcial | 60% | ⭐⭐⭐ |
| **Presupuesto** | ❌ No implementado | 0% | ⭐ |
| **Balance** | ❌ No implementado | 0% | ⭐ |
| **Frontend** | ❌ No implementado | 0% | ⭐ |
| **Validaciones** | ⚠️ Parcial | 70% | ⭐⭐⭐ |
| **Seguridad** | ⚠️ Parcial | 60% | ⭐⭐⭐ |

---
**Conclusión**: El módulo de gastos tiene una base sólida en el backend (entidad, repositorio, servicio), pero **falta la implementación crítica del endpoint POST /api/gastos y todo el frontend**. Además, hay **problemas de diseño en los DTOs** (campos faltantes) y **validaciones incompletas** (participación en viaje, estado del viaje, divisas). El módulo de presupuesto y balance **no existe**. **Priorizar Fases 1 y 2 antes de cualquier despliegue**.

</details>

## 2.3 Diferencias entre versiones

`git diff --numstat e72d585 d8fd7f0`

| Agregadas | Eliminadas | Archivo                                                                         |
| --------- | ---------- | ------------------------------------------------------------------------------- |
| 10        | 1          | backend/src/main/java/com/bitacora/viajes/controller/GastoController.java       |
| 13        | 0          | backend/src/main/java/com/bitacora/viajes/dto/GastoRequest.java                 |
| 8         | 3          | backend/src/main/java/com/bitacora/viajes/dto/GastoResponse.java                |
| 9         | 9          | backend/src/main/java/com/bitacora/viajes/exception/GlobalExceptionHandler.java |
| 6         | 0          | backend/src/main/java/com/bitacora/viajes/repository/GastoRepository.java       |
| 72        | 4          | backend/src/main/java/com/bitacora/viajes/service/GastoService.java             |

## 2.4 Prueba de funcionamiento

Las pruebas se hicieron con CURL desde WSL contra el servidor local (puerto 8081), después de aplicar las correcciones del commit d8fd7f0. Se usaron dos sesiones: ana@example.com, para gasto de prueba (id 3), y otro@example.com, un usuario distinto sin relación con ese gasto.

| N.º | Prueba                                  | Petición                         | Esperado                    | Obtenido                                           |
| --- | --------------------------------------- | -------------------------------- | --------------------------- | -------------------------------------------------- |
| 1   | Inicio de sesión del autor              | POST /api/auth/login             | 200                         | 200, devuelve el usuario Ana López                 |
| 2   | Actualizar gasto propio, datos válidos  | PUT /api/gastos/3                | 200                         | 200, gasto actualizado a 350.00 y totalViaje 550.0 |
| 3   | Comprobar el cambio en la base de datos | SELECT en la tabla gasto         | monto 350.00                | monto 350.00                                       |
| 4   | Monto igual a cero                      | PUT /api/gastos/3                | Rechazo por datos inválidos | "El monto debe ser mayor a cero"                   |
| 5   | Gasto inexistente                       | PUT /api/gastos/999999           | Rechazo por no encontrado   | "Gasto no encontrado"                              |
| 6   | Usuario sin permiso                     | PUT /api/gastos/3 (otro usuario) | Rechazo por permisos        | "No tienes permiso para modificar este gasto"      |
| 7   | Eliminar gasto propio                   | DELETE /api/gastos/3             | 204                         | 204, sin contenido                                 |

Las dos primeras peticiones PUT de la salida (400 y 404) se debieron a un error al escribir el comando (id literal y variable vacía), por lo que no se consideran pruebas.

## 2.5 Reflexión del estudiante

Vibe revisó los archivos del módulo de gastos y encontró discrepancias a nivel de capas: el dto de entrada no incluía el viajeId y los campos de divisas, la respuesta no devolvía el viajeId y el manejo de excepciones no utilizaba el dto de error del proyecto. Los correcciones las realicé en el commit d8fd7f0. Los hallazgos que se marcaron como críticos (presupuesto, balance y frontend) no eran errores, sino funcionalidad que estaba prevista para entregas posteriores, así que los dejé apartados. También contrasté sus propuestas antes de aplicarlas: por ejemplo, algunas afirmaciones no se podían contrastar por solo leer el código, caso del estado de la base de datos MySQL. He aprendido que la IA es útil para encontrar discrepancias entre capas, pero que los hallazgos deben ser contrastados con el código y con las pruebas antes de aceptar sus conclusiones.

---

## Observaciones adicionales

El análisis de Vibe incluyó funcionalidad aún no implementada (presupuesto, balance y frontend). No se consideraron errores, sino tareas para las próximas entregas.

# Wireframes de la Bitácora de viajes

Autora: Citlaly Morales Viveros

Diseño inicial de las pantallas. Cada wireframe muestra un prototipo de la distribución de los botonoes y acciones del sistema, sin colores ni estilos finales.

## 1. Login y registro

![Wireframe de login y registro](01-login-y-registro.png)

Pantalla de acceso que contiene el formulario de inicio de sesión (credenciales) y un enlace "Crear cuenta". Junto a ella se muestra cómo sería la variante registro, con nombre, correo, contraseña de mínimo 8 caracteres. El wireframe indica dónde aparecerán los error "Correo o contraseña incorrectos" y los errores bajo cada campo.

## 2. Lista de viajes

![Wireframe de la lista de viajes](02-lista-de-viajes.png)

Pantalla de "Mis viajes" con título, botón "Nuevo viaje" y filtros por destino y por rol. Los viajes se muestran en una cuadrícula, cada una con destino, fechas, presupuesto con moneda (tipo) e insignia de rol (organizador o participante). Incluye una variante de estado vacío con el texto "Aún no tienes viajes" y un botón para crear el primer viaje.

## 3. Crear viaje

![Wireframe de crear viaje](03-crear-viaje.png)

Formulario con destino, fecha de inicio, fecha de fin, presupuesto total y selector de moneda (MXN por defecto), con los botones "Guardar viaje" y "Cancelar". Muestra los errores en línea bajo cada campo (fecha de fin anterior al inicio, presupuesto mayor a 0) y la zona de mensajes.

## 4. Detalle de viaje

![Wireframe del detalle de viaje](04-detalle-de-viaje.png)

Pantalla en dos columnas. La izquierda tiene el encabezado con destino, fechas y participantes con su rol, y debajo una tarjeta por cada día. Cada tarjeta lleva una pestaña lateral con el número y mes del día, y la lista de actividades ordenadas por hora (hora, título, costo estimado y botón de voto con contador). Uno de los días aparece vacío con el texto "Sin actividades". La columna derecha contiene el panel de presupuesto, balance y checklist. Los botones principales son "Proponer actividad", "Exportar a PDF" y "Editar viaje".

## 5. Votación

![Wireframe de votación](05-votacion.png)

Detalle de una tarjeta de día enfocada en la votación, con 3 actividades y su botón de voto con contador. Se muestra el estado "ya votaste" en una actividad y "sin votar" en otra, además de la insignia "Más votada" en la actividad ganadora. También indica el estado del botón mientras la petición está en curso.

## 6. Gastos

![Wireframe de gastos](06-gastos.png)

Sección con un formulario de concepto, categoría (comida, transporte, hospedaje, actividades, otros), monto y moneda, con una línea "Equivale a ... MXN al tipo de cambio de hoy" y un campo opcional para adjuntar comprobante. Debajo hay una tabla con concepto, categoría, quién pagó, monto, fecha, icono de comprobante y acciones de editar y eliminar, con los gastos más recientes primero. Muestra la zona de mensajes y el aviso cuando falla el tipo de cambio.

## 7. Presupuesto

![Wireframe de presupuesto](07-presupuesto.png)

Componente de resumen con presupuesto total, gastado, restante, porcentaje usado y gasto por persona, junto con una barra de progreso. Se muestran tres versiones apiladas de la barra: menos de 75% (verde), de 75 a 100% (amarillo) y más de 100% (rojo, barra llena y aviso "Presupuesto excedido").

## 8. Balance del grupo

![Wireframe del balance del grupo](08-deudas.png)

Tabla con participante, pagado, cuota y saldo, donde los saldos a favor se marcan en verde y las deudas en rojo. Debajo aparece la lista de transferencias sugeridas con el formato "Ana paga a Luis $350.00". Incluye el estado "Todo está saldado".

## 9. Checklist

![Wireframe del checklist](09-checklist.png)

Lista de tareas con el contador "3 de 8 completadas", un campo para agregar tarea con botón "Agregar", y cada tarea con su casilla, su texto (tachado si está completada) y un botón de eliminar. Indica dónde aparece el error si falla el cambio de estado.

## 10. Comprobante de gastos

![Wireframe del comprobante de gastos](10-comprobante-de-gastos.png)

Ventana modal sobre la tabla de gastos, con título "Comprobante", imagen grande del comprobante y los datos del gasto (concepto, monto y quién pagó). Tiene un botón de cerrar arriba a la derecha y fondo oscurecido. Indica el estado de error "No tienes permiso para ver este comprobante".

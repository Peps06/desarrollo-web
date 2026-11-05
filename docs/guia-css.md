<!-- HU-06 | Responsable: Citlaly | Guía de estilos CSS: variables, convenciones y componentes. -->

# Guía de clases CSS

Autora: Citlaly Morales Viveros (Cliente / Programadora XP)

CSS propio, sin Bootstrap ni frameworks. Los estilos viven en `src/main/resources/static/css/`. Si una vista necesita un cambio de HTML, se le pide a quien sea responsable de ese archivo.

## Orden de carga

Va en `fragments/base.html` (responsabilidad de Stephy), en este orden:

```html
<link
  rel="stylesheet"
  href="https://fonts.googleapis.com/css2?family=Fraunces:wght@600&family=Source+Sans+3:wght@400;600&display=swap"
/>
<link rel="stylesheet" th:href="@{/css/variables.css}" />
<link rel="stylesheet" th:href="@{/css/base.css}" />
<link rel="stylesheet" th:href="@{/css/componentes.css}" />
<link rel="stylesheet" th:href="@{/css/presupuesto.css}" />
<link rel="stylesheet" th:href="@{/css/responsive.css}" />
```

Si no hay internet las fuentes caen a Georgia y Segoe UI/system-ui; no se rompe nada.

## Paleta

| Variable           | Color     | Uso                                           |
| ------------------ | --------- | --------------------------------------------- |
| `--azul-montana`   | `#294C60` | Barra de navegación, títulos, botón primario  |
| `--azul-grisaceo`  | `#477A91` | Enlaces, bordes de campos, botón de voto      |
| `--verde-glaciar`  | `#83B6B3` | Foco en campos, detalles, estado vacío        |
| `--naranja-calido` | `#E6A15C` | Filo de la barra superior e insignia ganadora |
| `--blanco-natural` | `#F3F6F3` | Fondo de página y texto sobre azul            |

Colores de estado (derivados, para que se distingan bien entre sí): `--estado-ok` verde, `--estado-alerta` amarillo, `--estado-peligro` rojo, cada uno con su variante `-suave` para fondos.

## Tipografía

- Títulos: Fraunces (`--fuente-titulo`), peso 600.
- Texto y formularios: Source Sans 3 (`--fuente-texto`).
- Escala: `--texto-xs` 13 px, `--texto-sm` 15 px, `--texto-base` 16 px, `--texto-lg` 20 px, `--texto-xl` 26 px, `--texto-2xl` 34 px.

## Layout

| Clase                                                | Para qué                                                              |
| ---------------------------------------------------- | --------------------------------------------------------------------- |
| `.contenedor`                                        | Ancho máximo y márgenes laterales                                     |
| `.pagina`, `.pagina__encabezado`                     | Cuerpo de la vista y fila con título y acción principal               |
| `.rejilla`                                           | Tarjetas en columnas (3 en laptop, 1 en tablet)                       |
| `.pila` / `.fila`                                    | Elementos en vertical / en horizontal con separación                  |
| `.detalle-viaje` + `.panel-lateral`                  | Detalle del viaje: itinerario y panel de gastos lado a lado en laptop |
| `.barra-nav` (`__contenido`, `__marca`, `__enlaces`) | Barra superior                                                        |

## Componentes

**Tarjetas**: `.tarjeta` (`__titulo`, `__meta`, modificador `--enlace` si toda la tarjeta es clicable). `.tarjeta-dia` con `__fecha` (`__numero`, `__mes`), `__contenido` y `__vacio` para un día sin actividades. Dentro del día, `.actividad` con `__hora`, `__info`, `__titulo`, `__detalle`; la ganadora lleva además `.actividad--ganadora`.

**Botones**: `.btn` es la base y siempre se combina con una variante: `.btn-primario`, `.btn-secundario`, `.btn-peligro`, `.btn-texto`. Tamaño chico: `.btn-pequeno`. Voto: `.btn-voto` con `.votado` cuando la persona ya votó y `.btn-voto__contador` para el número. Mientras hay una petición en curso se deshabilita con el atributo `disabled`.

**Formularios**: `.formulario` (`--ancho`, `__fila`), `.campo` con `__etiqueta`, `__ayuda`, `__error`. Un campo con error lleva `.campo--invalido`. Gasto en moneda extranjera: `.selector-moneda` (monto + moneda) y `.equivalente-cambio` para mostrar el equivalente.

**Tablas**: `.tabla-contenedor` envuelve a `.tabla`; las columnas de montos llevan `.numero`.

**Mensajes**: `.contenedor-mensajes` se crea una vez en la página y `mostrarMensaje(tipo, texto)` agrega dentro un `.mensaje` con `.mensaje-exito`, `.mensaje-error` o `.mensaje-aviso`. Desaparecen a los 4 segundos.

**Insignias**: `.insignia` base, `.insignia-ganadora` (actividad más votada, naranja), `.insignia-organizador`.

**Balance**: `.saldo-favor` (verde), `.saldo-deuda` (rojo), `.saldo-cero`, `.transferencia` por cada pago sugerido.

**Checklist**: `.checklist` > `.tarea` (`__texto`); al marcar se agrega `.completada` y el texto se tacha. `.contador-tareas` para "X de Y completadas".

**Modal**: `.modal-fondo` > `.modal` (`__cabecera`); imagen del comprobante con `.comprobante-imagen`.

**Otros**: `.vacio` para listas sin datos, `.acceso` para login y registro, utilidades `.texto-suave`, `.texto-centrado`, `.oculto`, `.solo-lector`.

## Barra de presupuesto

```html
<div
  class="barra-presupuesto estado-ok"
  style="--uso: 62%"
  role="progressbar"
  aria-valuenow="62"
  aria-valuemin="0"
  aria-valuemax="100"
>
  <span class="barra-presupuesto__relleno"></span>
</div>
```

| Porcentaje usado | Clase             | Color                                              |
| ---------------- | ----------------- | -------------------------------------------------- |
| menor a 75       | `estado-ok`       | verde                                              |
| de 75 a 100      | `estado-alerta`   | amarillo                                           |
| mayor a 100      | `estado-excedido` | rojo, `--uso: 100%` y se muestra `.aviso-excedido` |

Ejemplo de la lógica en JavaScript:

```js
function estadoBarra(porcentaje) {
  if (porcentaje > 100) return "estado-excedido";
  if (porcentaje >= 75) return "estado-alerta";
  return "estado-ok";
}
```

## Responsive

- Laptop (`min-width: 1024px`): rejilla de 3 columnas y detalle en dos columnas.
- Tablet (`max-width: 768px`): una columna, tarjeta-dia con la fecha arriba, botones de 44 px de alto, modal a pantalla completa.

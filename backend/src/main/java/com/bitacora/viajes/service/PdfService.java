package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ActividadMasVotadaResponse;
import com.bitacora.viajes.dto.ActividadResponse;
import com.bitacora.viajes.dto.DiaResponse;
import com.bitacora.viajes.dto.ViajeDetalleResponse;
import com.bitacora.viajes.dto.ViajeDetalleResponse.Participante;
import com.bitacora.viajes.model.RolParticipante;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Genera el itinerario del viaje en PDF: datos generales, participantes, presupuesto
 * y las actividades de cada día ordenadas por hora. El archivo se arma en memoria.
 */
@Service
public class PdfService {

    private static final Locale ES_MX = Locale.forLanguageTag("es-MX");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DIA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES_MX);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_GENERADO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String SIN_ACTIVIDADES = "Sin actividades registradas";
    private static final int LARGO_MAXIMO_DESTINO = 60;

    // Colores tomados de la paleta de la interfaz (variables.css).
    private static final Color AZUL_MONTANA = new Color(0x29, 0x4C, 0x60);
    private static final Color GLACIAR_CLARO = new Color(0xE3, 0xEF, 0xEE);
    private static final Color NARANJA_CALIDO = new Color(0xE6, 0xA1, 0x5C);
    private static final Color TEXTO = new Color(0x1F, 0x33, 0x40);
    private static final Color TEXTO_SUAVE = new Color(0x4F, 0x68, 0x77);
    private static final Color BORDE = new Color(0xD3, 0xDE, 0xDA);
    private static final Color BLANCO = new Color(0xFF, 0xFF, 0xFF);

    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, AZUL_MONTANA);
    private static final Font FUENTE_DIA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.BOLD, AZUL_MONTANA);
    private static final Font FUENTE_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, TEXTO);
    private static final Font FUENTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, TEXTO);
    private static final Font FUENTE_SUAVE = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, TEXTO_SUAVE);
    private static final Font FUENTE_VACIO = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Font.ITALIC, TEXTO_SUAVE);
    private static final Font FUENTE_ENCABEZADO_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, BLANCO);
    private static final Font FUENTE_GANADORA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.BOLD, AZUL_MONTANA);

    private final ViajeDetalleService viajeDetalleService;

    public PdfService(ViajeDetalleService viajeDetalleService) {
        this.viajeDetalleService = viajeDetalleService;
    }

    /** PDF generado y el nombre con el que se descarga. */
    public record ItinerarioPdf(String nombreArchivo, byte[] contenido) {
    }

    // Reutiliza el detalle del viaje: valida 404 (no existe o cancelado) y 403 (no participa)
    // y carga participantes, actividades y votos con consultas agrupadas, sin N+1.
    public ItinerarioPdf generarItinerario(Integer viajeId, String correoUsuario) {
        ViajeDetalleResponse viaje = viajeDetalleService.obtenerDetalle(viajeId, correoUsuario);
        return new ItinerarioPdf(nombreArchivo(viaje.getDestino()), construirPdf(viaje));
    }

    // Nombre del archivo con el destino sin acentos ni caracteres inválidos.
    // Ejemplo: "Oaxaca, Oaxaca" -> "itinerario-Oaxaca-Oaxaca.pdf".
    static String nombreArchivo(String destino) {
        String sinAcentos = Normalizer.normalize(destino == null ? "" : destino, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String limpio = sinAcentos.replaceAll("[^A-Za-z0-9_-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        if (limpio.length() > LARGO_MAXIMO_DESTINO) {
            limpio = limpio.substring(0, LARGO_MAXIMO_DESTINO).replaceAll("-$", "");
        }
        if (limpio.isEmpty()) {
            limpio = "viaje";
        }
        return "itinerario-" + limpio + ".pdf";
    }

    private byte[] construirPdf(ViajeDetalleResponse viaje) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.addTitle("Itinerario - " + viaje.getDestino());
            documento.addCreator("Bitácora de viajes colaborativa");
            documento.open();

            agregarEncabezado(documento, viaje);
            for (DiaResponse dia : viaje.getDias()) {
                agregarDia(documento, dia, viaje.getMonedaBase());
            }

            Paragraph pie = new Paragraph("Generado el " + LocalDateTime.now().format(FORMATO_GENERADO), FUENTE_SUAVE);
            pie.setSpacingBefore(18);
            pie.setAlignment(Element.ALIGN_RIGHT);
            documento.add(pie);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del itinerario", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return salida.toByteArray();
    }

    // Destino, fechas, participantes y resumen del presupuesto.
    private void agregarEncabezado(Document documento, ViajeDetalleResponse viaje) throws DocumentException {
        Paragraph titulo = new Paragraph("Itinerario: " + viaje.getDestino(), FUENTE_TITULO);
        titulo.setSpacingAfter(8);
        documento.add(titulo);

        long totalDias = viaje.getFechaInicio().datesUntil(viaje.getFechaFin().plusDays(1)).count();
        documento.add(linea("Fechas: ",
                "del " + viaje.getFechaInicio().format(FORMATO_FECHA) + " al "
                        + viaje.getFechaFin().format(FORMATO_FECHA)
                        + " (" + totalDias + (totalDias == 1 ? " día)" : " días)")));

        String participantes = viaje.getParticipantes().stream()
                .map(PdfService::textoParticipante)
                .collect(Collectors.joining(", "));
        documento.add(linea("Participantes (" + viaje.getParticipantes().size() + "): ", participantes));

        BigDecimal presupuesto = valor(viaje.getPresupuestoTotal());
        BigDecimal gastado = valor(viaje.getTotalGastado());
        String moneda = viaje.getMonedaBase();
        documento.add(linea("Presupuesto: ",
                formatearMoneda(presupuesto, moneda) + "   |   Gastado: " + formatearMoneda(gastado, moneda)
                        + "   |   Restante: " + formatearMoneda(presupuesto.subtract(gastado), moneda)));
    }

    // Título del día y su tabla de actividades, o el aviso de día vacío.
    private void agregarDia(Document documento, DiaResponse dia, String moneda) throws DocumentException {
        Paragraph titulo = new Paragraph(capitalizar(dia.fecha().format(FORMATO_DIA)), FUENTE_DIA);
        titulo.setSpacingBefore(16);
        titulo.setSpacingAfter(6);
        documento.add(titulo);

        List<ActividadResponse> actividades = dia.actividades() == null ? List.of() : dia.actividades().stream()
                .sorted(Comparator.comparing(ActividadResponse::hora).thenComparing(ActividadResponse::id))
                .toList();

        if (actividades.isEmpty()) {
            Paragraph vacio = new Paragraph(SIN_ACTIVIDADES, FUENTE_VACIO);
            vacio.setIndentationLeft(8);
            documento.add(vacio);
            return;
        }

        ActividadMasVotadaResponse ganadora = dia.actividadMasVotada();
        Integer idGanadora = ganadora == null ? null : ganadora.id();

        PdfPTable tabla = new PdfPTable(new float[] {1.2f, 6.3f, 2.5f});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        tabla.addCell(celdaEncabezado("Hora", Element.ALIGN_LEFT));
        tabla.addCell(celdaEncabezado("Actividad", Element.ALIGN_LEFT));
        tabla.addCell(celdaEncabezado("Costo estimado", Element.ALIGN_RIGHT));

        for (ActividadResponse actividad : actividades) {
            boolean esGanadora = actividad.id().equals(idGanadora);
            tabla.addCell(celda(new Phrase(actividad.hora().format(FORMATO_HORA), FUENTE_ETIQUETA),
                    Element.ALIGN_LEFT, esGanadora));
            tabla.addCell(celda(textoActividad(actividad, esGanadora), Element.ALIGN_LEFT, esGanadora));
            tabla.addCell(celda(new Phrase(formatearMoneda(valor(actividad.costoEstimado()), moneda), FUENTE_TEXTO),
                    Element.ALIGN_RIGHT, esGanadora));
        }
        documento.add(tabla);
    }

    // Título de la actividad, marca de más votada y, si hay, su descripción.
    private static Phrase textoActividad(ActividadResponse actividad, boolean esGanadora) {
        Phrase texto = new Phrase(actividad.titulo(), FUENTE_ETIQUETA);
        if (esGanadora) {
            texto.add(new Chunk("   MÁS VOTADA (" + actividad.totalVotos() + ")", FUENTE_GANADORA));
        }
        if (actividad.descripcion() != null && !actividad.descripcion().isBlank()) {
            texto.add(new Chunk("\n" + actividad.descripcion(), FUENTE_SUAVE));
        }
        return texto;
    }

    private static PdfPCell celdaEncabezado(String texto, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_ENCABEZADO_TABLA));
        celda.setBackgroundColor(AZUL_MONTANA);
        celda.setBorderColor(AZUL_MONTANA);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(6);
        return celda;
    }

    private static PdfPCell celda(Phrase contenido, int alineacion, boolean esGanadora) {
        PdfPCell celda = new PdfPCell(contenido);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setBorderColor(esGanadora ? NARANJA_CALIDO : BORDE);
        celda.setPadding(6);
        if (esGanadora) {
            celda.setBackgroundColor(GLACIAR_CLARO);
        }
        return celda;
    }

    // Párrafo con una etiqueta en negritas y su valor.
    private static Paragraph linea(String etiqueta, String valor) {
        Paragraph parrafo = new Paragraph();
        parrafo.add(new Chunk(etiqueta, FUENTE_ETIQUETA));
        parrafo.add(new Chunk(valor, FUENTE_TEXTO));
        parrafo.setSpacingAfter(3);
        return parrafo;
    }

    private static String textoParticipante(Participante participante) {
        return participante.rol() == RolParticipante.ORGANIZADOR
                ? participante.nombre() + " (organizador)"
                : participante.nombre();
    }

    // Monto con separador de miles y dos decimales, por ejemplo "$15,000.00 MXN".
    private static String formatearMoneda(BigDecimal monto, String moneda) {
        NumberFormat formato = NumberFormat.getNumberInstance(ES_MX);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        String signo = monto.signum() < 0 ? "-" : "";
        return signo + "$" + formato.format(monto.abs()) + " " + (moneda == null ? "MXN" : moneda);
    }

    private static BigDecimal valor(BigDecimal monto) {
        return monto == null ? BigDecimal.ZERO : monto;
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : texto.substring(0, 1).toUpperCase(ES_MX) + texto.substring(1);
    }
}

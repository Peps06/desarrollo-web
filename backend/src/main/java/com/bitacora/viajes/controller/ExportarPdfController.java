package com.bitacora.viajes.controller;

import com.bitacora.viajes.service.PdfService;
import com.bitacora.viajes.service.PdfService.ItinerarioPdf;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/viajes")
public class ExportarPdfController {

    private final PdfService pdfService;

    public ExportarPdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    // Descarga el itinerario del viaje en PDF. El usuario sale siempre de la sesión.
    // 404 si el viaje no existe o está cancelado y 403 si el usuario no participa.
    @GetMapping("/{id}/exportar-pdf")
    public ResponseEntity<byte[]> exportarPdf(@PathVariable Integer id, Authentication autenticacion) {
        ItinerarioPdf pdf = pdfService.generarItinerario(id, autenticacion.getName());
        ContentDisposition descarga = ContentDisposition.attachment().filename(pdf.nombreArchivo()).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, descarga.toString())
                .contentLength(pdf.contenido().length)
                .body(pdf.contenido());
    }
}

// HU-25 | Responsable: Citlaly | Guarda y recupera los comprobantes de gasto en disco.
package com.bitacora.viajes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;

/**
 * Manejo de los archivos de comprobantes en disco.
 *
 * @author Citlaly Morales Viveros
 */
@Service
public class AlmacenamientoService {

    /**
     * Borra un comprobante del disco. Si el archivo ya no existe, no hace nada.
     */
    public void eliminar(String rutaComprobante) {
        try {
            Files.deleteIfExists(Paths.get(rutaComprobante));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo borrar el comprobante", e);
        }
    }
}

// HU-25 | Responsable: Citlaly | Guarda y recupera los comprobantes de gasto en disco.
package com.bitacora.viajes.service;

import com.bitacora.viajes.exception.DatosInvalidosException;
import com.bitacora.viajes.exception.RecursoNoEncontradoException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manejo de los archivos de comprobantes en disco.
 *
 * Autora: Citlaly Morales Viveros
 */
@Service
public class AlmacenamientoService {

    private static final long TAMANO_MAXIMO = 2L * 1024 * 1024;

    // Tipos de imagen permitidos y la extensión con la que se guardan
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    @Value("${app.uploads.dir:uploads}")
    private String carpeta;

    /**
     * Guarda una imagen con nombre único y regresa la ruta relativa que se guarda en la base de datos.
     */
    public String guardar(MultipartFile archivo) {
        String tipo = archivo.getContentType();
        if (tipo == null || !EXTENSIONES.containsKey(tipo)) {
            throw new DatosInvalidosException("El comprobante debe ser una imagen jpg, png o webp");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new DatosInvalidosException("El comprobante no puede pesar más de 2 MB");
        }

        String nombre = UUID.randomUUID() + EXTENSIONES.get(tipo);
        Path destino = Paths.get(carpeta).resolve(nombre);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el comprobante", e);
        }
        return carpeta + "/" + nombre;
    }

    /**
     * Regresa el archivo guardado para poder mostrarlo. Si ya no existe, da 404.
     */
    public Resource cargar(String rutaComprobante) {
        Path ruta = Paths.get(rutaComprobante);
        if (!Files.exists(ruta)) {
            throw new RecursoNoEncontradoException("El comprobante ya no existe");
        }
        return new FileSystemResource(ruta);
    }

    /**
     * Regresa el tipo de contenido según la extensión del archivo.
     */
    public String tipoContenido(String nombreArchivo) {
        String minusculas = nombreArchivo.toLowerCase();
        if (minusculas.endsWith(".png")) {
            return "image/png";
        }
        if (minusculas.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

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

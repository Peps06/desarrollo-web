package com.bitacora.viajes.exception;

import com.bitacora.viajes.dto.ErrorCampo;
import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import com.bitacora.viajes.service.ViajeService.ValidacionViajeException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Convierte las excepciones de negocio en respuestas HTTP con el formato ErrorResponse.
 *
 * @author Citlaly Morales Viveros
 * @author Dana Carmona Santamaria
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** El recurso pedido no existe: 404. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** El usuario no tiene permiso para la acción: 403. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> sinPermiso(AccesoDenegadoException e) {
        return respuesta(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /** Los datos no cumplen una regla de negocio: 400. */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(DatosInvalidosException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Error de negocio que trae su propio código HTTP (404, 403 o 400). */
    @ExceptionHandler(ErrorNegocioException.class)
    public ResponseEntity<ErrorResponse> errorNegocio(ErrorNegocioException e) {
        return respuesta(e.getEstado(), e.getMessage());
    }

    /** Error de validación de viaje, con el campo que falló: 400. */
    @ExceptionHandler(ValidacionViajeException.class)
    public ResponseEntity<ErrorResponse> validacionViaje(ValidacionViajeException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.validacion(e.getCampo(), e.getMessage()));
    }

    /** Campos de la petición que no pasaron las validaciones: 400 con la lista de campos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        List<ErrorCampo> errores = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorCampo(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "Datos inválidos", errores));
    }

    /** El cuerpo no es un JSON válido: 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido");
    }

    /** Un parámetro de la ruta tiene un tipo incorrecto, por ejemplo un id que no es número: 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoIncorrecto(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.validacion(e.getName(), "El valor no es válido"));
    }

    /** La ruta no existe: 404. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoExiste(NoResourceFoundException e) {
        return respuesta(HttpStatus.NOT_FOUND, "El recurso no existe");
    }

    /** El método HTTP no se acepta en esa ruta: 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "El método no está permitido en esta ruta");
    }

    /** El tipo de contenido enviado no se acepta: 415. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tipoNoSoportado(HttpMediaTypeNotSupportedException e) {
        return respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El tipo de contenido no es compatible");
    }

    /** Cualquier otro error: 500 con mensaje genérico. El detalle solo va al registro del servidor. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception e) {
        log.error("Error inesperado en la API", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intenta de nuevo más tarde");
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(estado.value(), mensaje));
    }
}
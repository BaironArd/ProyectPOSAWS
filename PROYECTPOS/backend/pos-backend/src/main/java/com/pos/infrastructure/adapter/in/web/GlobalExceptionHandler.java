package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.exception.*;
import com.pos.infrastructure.adapter.in.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ProductoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("PRODUCTO_NO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(VentaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(VentaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("VENTA_NO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handle(StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("STOCK_INSUFICIENTE", ex.getMessage()));
    }

    @ExceptionHandler(MontoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handle(MontoInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("VENTA_MONTO_INSUFICIENTE", ex.getMessage()));
    }

    @ExceptionHandler(CarritoVacioException.class)
    public ResponseEntity<ErrorResponse> handle(CarritoVacioException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("CARRITO_VACIO", ex.getMessage()));
    }

    @ExceptionHandler(QueryDemasiadoCortaException.class)
    public ResponseEntity<ErrorResponse> handle(QueryDemasiadoCortaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("QUERY_DEMASIADO_CORTA", ex.getMessage()));
    }

    @ExceptionHandler(ConflictoStockException.class)
    public ResponseEntity<ErrorResponse> handle(ConflictoStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("CONFLICTO_STOCK", ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handle(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("CREDENCIALES_INVALIDAS", ex.getMessage()));
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handle(TokenInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("TOKEN_INVALIDO", ex.getMessage()));
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> handle(AccesoDenegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("ACCESO_DENEGADO", ex.getMessage()));
    }

    @ExceptionHandler(VentaYaDevueltaException.class)
    public ResponseEntity<ErrorResponse> handle(VentaYaDevueltaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("VENTA_YA_DEVUELTA", ex.getMessage()));
    }

    @ExceptionHandler(VentaNoDevolvibleException.class)
    public ResponseEntity<ErrorResponse> handle(VentaNoDevolvibleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("VENTA_NO_DEVOLVIBLE", ex.getMessage()));
    }

    @ExceptionHandler(ProductoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handle(ProductoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("PRODUCTO_DUPLICADO", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDACION_FALLIDA", mensaje));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("ERROR_INTERNO", "Ha ocurrido un error interno. Por favor intenta nuevamente."));
    }
}

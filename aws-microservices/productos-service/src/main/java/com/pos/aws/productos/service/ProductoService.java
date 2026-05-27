package com.pos.aws.productos.service;

import com.pos.aws.productos.model.ProductoDynamo;
import com.pos.aws.productos.model.ProductoResponse;
import com.pos.aws.productos.repository.ProductoRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio Productos.
 *
 * Responsabilidades:
 *   - Validar los parámetros de búsqueda recibidos desde el Handler.
 *   - Delegar la consulta al repositorio DynamoDB.
 *   - Mapear entidades DynamoDB a DTOs de respuesta.
 *
 * NO contiene lógica de HTTP ni de infraestructura AWS.
 */
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca productos según el tipo de búsqueda y el valor proporcionado.
     *
     * @param tipoBusqueda  "id" | "codigoBarras" | "codigo" | "nombre" | "todos"
     * @param valor         el valor a buscar (puede ser null para "todos")
     * @return lista de productos encontrados como DTOs
     * @throws IllegalArgumentException si el tipo de búsqueda no es válido o el valor está vacío
     */
    public List<ProductoResponse> buscar(String tipoBusqueda, String valor) {
        validarParametros(tipoBusqueda, valor);

        List<ProductoDynamo> resultados = switch (tipoBusqueda.toLowerCase()) {
            case "id" -> repository.buscarPorId(valor)
                    .map(List::of)
                    .orElse(List.of());
            case "codigobarras" -> repository.buscarPorCodigoBarras(valor);
            case "codigo"       -> repository.buscarPorCodigo(valor);
            case "nombre"       -> repository.buscarPorNombre(valor);
            case "todos"        -> repository.listarTodos();
            default -> throw new IllegalArgumentException(
                    "Tipo de búsqueda no válido: " + tipoBusqueda
                            + ". Valores permitidos: id, codigoBarras, codigo, nombre, todos");
        };

        return resultados.stream()
                .map(ProductoResponse::from)
                .collect(Collectors.toList());
    }

    // ---- Validaciones ----

    private void validarParametros(String tipoBusqueda, String valor) {
        if (tipoBusqueda == null || tipoBusqueda.isBlank()) {
            throw new IllegalArgumentException(
                    "El parámetro 'tipo' es requerido. "
                    + "Valores permitidos: id, codigoBarras, codigo, nombre, todos");
        }

        // "todos" no requiere valor
        if ("todos".equalsIgnoreCase(tipoBusqueda)) {
            return;
        }

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El parámetro 'q' es requerido cuando tipo != 'todos'");
        }

        if (valor.length() < 1) {
            throw new IllegalArgumentException(
                    "El valor de búsqueda debe tener al menos 1 carácter");
        }
    }
}

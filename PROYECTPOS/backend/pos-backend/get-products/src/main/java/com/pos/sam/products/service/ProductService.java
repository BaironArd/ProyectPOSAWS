package com.pos.sam.products.service;

import com.pos.sam.products.model.ProductRecord;
import com.pos.sam.products.repository.ProductRepository;

import java.util.List;

/**
 * Servicio de productos.
 * Contiene validaciones y lógica de búsqueda.
 * No sabe nada de HTTP ni de AWS Lambda.
 */
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca productos según el tipo y valor.
     *
     * @param type  "id" | "code" | "name" | "all"
     * @param value valor a buscar (null permitido solo para "all")
     */
    public List<ProductRecord> search(String type, String value) {
        if (type == null || type.isBlank()) {
            type = "all";
        }

        return switch (type.toLowerCase()) {
            case "id"   -> repository.findById(requireValue(value, "id"))
                    .map(List::of).orElse(List.of());
            case "code" -> repository.findByCode(requireValue(value, "code"));
            case "name" -> repository.findByName(requireValue(value, "name"));
            case "all"  -> repository.findAll();
            default     -> throw new IllegalArgumentException(
                    "Tipo de búsqueda inválido: '" + type + "'. Use: id, code, name, all");
        };
    }

    private String requireValue(String value, String type) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "El parámetro 'q' es requerido cuando type='" + type + "'");
        }
        return value;
    }
}

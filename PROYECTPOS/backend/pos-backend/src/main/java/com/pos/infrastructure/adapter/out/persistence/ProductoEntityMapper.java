package com.pos.infrastructure.adapter.out.persistence;

import com.pos.domain.model.Dinero;
import com.pos.domain.model.Producto;

public class ProductoEntityMapper {

    public Producto toDomain(ProductoEntity entity) {
        return new Producto(
                entity.getId(),
                entity.getNombre(),
                Dinero.dePesos(entity.getPrecio()),
                entity.getStock(),
                entity.getCategoria(),
                entity.isActivo()
        );
    }

    public ProductoEntity toEntity(Producto domain) {
        ProductoEntity entity = new ProductoEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setPrecio(domain.getPrecio().toPesos());
        entity.setStock(domain.getStock());
        entity.setCategoria(domain.getCategoria());
        entity.setActivo(domain.isActivo());
        return entity;
    }
}

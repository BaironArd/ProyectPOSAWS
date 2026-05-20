package com.pos.infrastructure.adapter.out.persistence;

import com.pos.domain.exception.ConflictoStockException;
import com.pos.domain.model.Producto;
import com.pos.domain.port.out.ProductoRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@SuppressWarnings("null")
public class ProductoJpaAdapter implements ProductoRepository {

    private final ProductoJpaRepository jpaRepository;
    private final ProductoEntityMapper mapper;

    public ProductoJpaAdapter(ProductoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = new ProductoEntityMapper();
    }

    @Override
    public List<Producto> buscarPorNombre(String query) {
        return jpaRepository.findByNombreContainingIgnoreCaseAndActivoTrue(query)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Producto> buscarPorNombreOCodigo(String query) {
        List<Producto> resultados = new ArrayList<>(buscarPorNombre(query));
        if (query.matches("\\d+")) {
            Long id = Long.parseLong(query);
            jpaRepository.findById(id)
                    .filter(ProductoEntity::isActivo)
                    .map(mapper::toDomain)
                    .ifPresent(producto -> {
                        boolean existe = resultados.stream()
                                .anyMatch(p -> p.getId().equals(producto.getId()));
                        if (!existe) {
                            resultados.add(producto);
                        }
                    });
        }
        return resultados;
    }

    @Override
    public List<Producto> buscarPorNombrePaginado(String query, int page, int size) {
        return jpaRepository.findByNombreContainingIgnoreCaseAndActivoTrue(
                        query, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long contarPorNombre(String query) {
        return jpaRepository.countByNombreContainingIgnoreCaseAndActivoTrue(query);
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Producto> findByNombreIgnoreCase(String nombre) {
        return jpaRepository.findByNombreIgnoreCase(nombre).map(mapper::toDomain);
    }

    @Override
    public Producto save(Producto producto) {
        ProductoEntity entity;
        if (producto.getId() != null) {
            // Actualización: cargar la entidad existente para preservar @Version
            entity = jpaRepository.findById(producto.getId())
                    .orElse(new ProductoEntity());
        } else {
            entity = new ProductoEntity();
        }
        // Actualizar campos sin tocar el campo version (lo gestiona JPA)
        entity.setNombre(producto.getNombre());
        entity.setPrecio(producto.getPrecio().toPesos());
        entity.setStock(producto.getStock());
        entity.setCategoria(producto.getCategoria());
        entity.setActivo(producto.isActivo());

        ProductoEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void saveAll(List<Producto> productos) {
        try {
            // Para saveAll también preservamos la versión cargando las entidades existentes
            List<ProductoEntity> entities = productos.stream().map(p -> {
                ProductoEntity entity = p.getId() != null
                        ? jpaRepository.findById(p.getId()).orElse(new ProductoEntity())
                        : new ProductoEntity();
                entity.setNombre(p.getNombre());
                entity.setPrecio(p.getPrecio().toPesos());
                entity.setStock(p.getStock());
                entity.setCategoria(p.getCategoria());
                entity.setActivo(p.isActivo());
                return entity;
            }).toList();
            jpaRepository.saveAll(entities);
        } catch (OptimisticLockException e) {
            throw new ConflictoStockException();
        }
    }
}

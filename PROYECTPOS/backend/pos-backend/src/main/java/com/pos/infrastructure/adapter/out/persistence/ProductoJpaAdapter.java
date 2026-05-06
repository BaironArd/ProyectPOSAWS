package com.pos.infrastructure.adapter.out.persistence;

import com.pos.domain.exception.ConflictoStockException;
import com.pos.domain.model.Producto;
import com.pos.domain.port.out.ProductoRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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
        ProductoEntity saved = jpaRepository.save(mapper.toEntity(producto));
        return mapper.toDomain(saved);
    }

    @Override
    public void saveAll(List<Producto> productos) {
        try {
            jpaRepository.saveAll(productos.stream().map(mapper::toEntity).toList());
        } catch (OptimisticLockException e) {
            throw new ConflictoStockException();
        }
    }
}

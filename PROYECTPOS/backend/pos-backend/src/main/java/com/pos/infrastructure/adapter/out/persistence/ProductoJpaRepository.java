package com.pos.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    Page<ProductoEntity> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);

    long countByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    Optional<ProductoEntity> findByNombreIgnoreCase(String nombre);
}

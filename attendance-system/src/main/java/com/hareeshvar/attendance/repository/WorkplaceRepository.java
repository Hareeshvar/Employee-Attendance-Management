package com.hareeshvar.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hareeshvar.attendance.entity.Workplace;

public interface WorkplaceRepository extends JpaRepository<Workplace, Long>, JpaSpecificationExecutor<Workplace> {

    Optional<Workplace> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    boolean existsByCodeAndWorkplaceIdNot(String code, Long workplaceId);

    boolean existsByNameAndWorkplaceIdNot(String name, Long workplaceId);

    List<Workplace> findByIsActiveTrue();
}

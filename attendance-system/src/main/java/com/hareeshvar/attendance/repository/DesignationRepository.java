package com.hareeshvar.attendance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.Designation;

public interface DesignationRepository
        extends JpaRepository<Designation, Long> {

    Optional<Designation> findByDesignationName(String designationName);

    boolean existsByDesignationName(String designationName);

}
package com.hareeshvar.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hareeshvar.attendance.entity.Leave;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Leave> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Leave> findById(Long id);

}
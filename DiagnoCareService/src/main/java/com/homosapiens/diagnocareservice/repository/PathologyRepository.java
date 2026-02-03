package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.Pathology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PathologyRepository extends JpaRepository<Pathology, Long> {
    Optional<Pathology> findByPathologyName(String pathologyName);
    Optional<Pathology> findByPathologyNameContainingIgnoreCase(String pathologyName);
}

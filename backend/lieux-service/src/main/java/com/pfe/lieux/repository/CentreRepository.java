package com.pfe.lieux.repository;

import com.pfe.lieux.domain.Centre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CentreRepository extends JpaRepository<Centre, Long> {

    @Query("SELECT c FROM Centre c ORDER BY c.nomCentre ASC")
    List<Centre> findAllOrderByNom();

    boolean existsByNomCentreIgnoreCase(String nomCentre);

    boolean existsByNomCentreIgnoreCaseAndIdNot(String nomCentre, Long id);
}

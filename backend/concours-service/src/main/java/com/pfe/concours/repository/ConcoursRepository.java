package com.pfe.concours.repository;

import com.pfe.concours.domain.Concours;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcoursRepository extends JpaRepository<Concours, String> {

    @EntityGraph(attributePaths = "affectationsCentres")
    @Query("SELECT c FROM Concours c WHERE c.numeroConcours = :numeroConcours")
    Optional<Concours> findByIdWithAffectations(@Param("numeroConcours") String numeroConcours);

    @EntityGraph(attributePaths = "affectationsCentres")
    @Query("SELECT c FROM Concours c ORDER BY c.dateHeureExamen DESC")
    List<Concours> findAllWithAffectationsOrderByDateDesc();

    @Query(
            """
            SELECT DISTINCT c FROM Concours c
            JOIN c.affectationsCentres a
            WHERE a.idCentre = :idCentre
            ORDER BY c.nomConcours
            """)
    List<Concours> findDistinctByAffectationIdCentre(@Param("idCentre") Long idCentre);
}

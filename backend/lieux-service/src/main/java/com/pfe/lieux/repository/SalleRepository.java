package com.pfe.lieux.repository;

import com.pfe.lieux.domain.Salle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalleRepository extends JpaRepository<Salle, Long> {

    Optional<Salle> findByIdAndEtablissementId(Long id, Long etablissementId);

    @Query("SELECT s FROM Salle s JOIN FETCH s.etablissement e JOIN FETCH e.centre WHERE s.id = :id")
    Optional<Salle> findByIdWithEtablissementAndCentre(@Param("id") Long id);

    List<Salle> findByConcoursIdOrderByNomSalleAsc(Long concoursId);

    @Query(
            """
            SELECT DISTINCT s.concoursId FROM Salle s
            WHERE s.etablissement.id = :etablissementId AND s.concoursId IS NOT NULL
            ORDER BY s.concoursId
            """)
    List<Long> findDistinctConcoursIdsByEtablissementId(@Param("etablissementId") Long etablissementId);

    @Query(
            """
            SELECT DISTINCT s.concoursId FROM Salle s
            WHERE s.etablissement.centre.id = :centreId AND s.concoursId IS NOT NULL
            ORDER BY s.concoursId
            """)
    List<Long> findDistinctConcoursIdsByCentreId(@Param("centreId") Long centreId);

    boolean existsByEtablissementIdAndNomSalleIgnoreCase(Long etablissementId, String nom);

    boolean existsByEtablissementIdAndNomSalleIgnoreCaseAndIdNot(Long etablissementId, String nom, Long id);
}

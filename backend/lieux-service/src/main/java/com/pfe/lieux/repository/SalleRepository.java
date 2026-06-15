package com.pfe.lieux.repository;

import com.pfe.lieux.domain.Salle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalleRepository extends JpaRepository<Salle, Long> {

    Optional<Salle> findByIdSalleAndEtablissement_IdEtablissement(Long idSalle, Long etablissementId);

    @Query("SELECT s FROM Salle s JOIN FETCH s.etablissement e JOIN FETCH e.centre WHERE s.idSalle = :id")
    Optional<Salle> findByIdWithEtablissementAndCentre(@Param("id") Long id);

    List<Salle> findByNumeroConcoursOrderByNomSalleAsc(String numeroConcours);

    @Query(
            """
            SELECT DISTINCT s.numeroConcours FROM Salle s
            WHERE s.etablissement.idEtablissement = :etablissementId AND s.numeroConcours IS NOT NULL
            ORDER BY s.numeroConcours
            """)
    List<String> findDistinctConcoursNumerosByEtablissementId(@Param("etablissementId") Long etablissementId);

    @Query(
            """
            SELECT DISTINCT s.numeroConcours FROM Salle s
            WHERE s.etablissement.centre.idCentre = :centreId AND s.numeroConcours IS NOT NULL
            ORDER BY s.numeroConcours
            """)
    List<String> findDistinctConcoursNumerosByCentreId(@Param("centreId") Long centreId);

    boolean existsByEtablissement_IdEtablissementAndNomSalleIgnoreCase(Long etablissementId, String nom);

    boolean existsByEtablissement_IdEtablissementAndNomSalleIgnoreCaseAndIdSalleNot(
            Long etablissementId, String nom, Long idSalle);
}

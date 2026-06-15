package com.pfe.lieux.repository;

import com.pfe.lieux.domain.Etablissement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {

    @Query("SELECT COUNT(e) FROM Etablissement e WHERE e.centre.idCentre = :centreId")
    long countByCentreId(@Param("centreId") Long centreId);

    @Query(
            """
            SELECT DISTINCT e FROM Etablissement e
            LEFT JOIN FETCH e.salles
            WHERE e.centre.idCentre = :centreId
            ORDER BY e.nomEtablissement ASC
            """)
    List<Etablissement> findByCentreIdWithSalles(@Param("centreId") Long centreId);

    Optional<Etablissement> findByIdEtablissementAndCentre_IdCentre(Long idEtablissement, Long centreId);

    @Query(
            """
            SELECT DISTINCT e FROM Etablissement e
            LEFT JOIN FETCH e.salles s
            WHERE e.idEtablissement = :id
            ORDER BY s.nomSalle ASC
            """)
    Optional<Etablissement> findByIdWithSalles(@Param("id") Long id);

    boolean existsByCentre_IdCentreAndNomEtablissementIgnoreCase(Long centreId, String nom);

    boolean existsByCentre_IdCentreAndNomEtablissementIgnoreCaseAndIdEtablissementNot(
            Long centreId, String nom, Long idEtablissement);
}

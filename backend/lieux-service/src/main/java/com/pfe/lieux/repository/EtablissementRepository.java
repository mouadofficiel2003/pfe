package com.pfe.lieux.repository;

import com.pfe.lieux.domain.Etablissement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {

    @Query("SELECT COUNT(e) FROM Etablissement e WHERE e.centre.id = :centreId")
    long countByCentreId(@Param("centreId") Long centreId);

    List<Etablissement> findByCentreIdOrderByNomEtablissementAsc(Long centreId);

    @Query(
            """
            SELECT DISTINCT e FROM Etablissement e
            LEFT JOIN FETCH e.salles
            WHERE e.centre.id = :centreId
            ORDER BY e.nomEtablissement ASC
            """)
    List<Etablissement> findByCentreIdWithSalles(@Param("centreId") Long centreId);

    Optional<Etablissement> findByIdAndCentreId(Long id, Long centreId);

    @Query(
            """
            SELECT DISTINCT e FROM Etablissement e
            LEFT JOIN FETCH e.salles s
            WHERE e.id = :id
            ORDER BY s.nomSalle ASC
            """)
    Optional<Etablissement> findByIdWithSalles(@Param("id") Long id);

    boolean existsByCentreIdAndNomEtablissementIgnoreCase(Long centreId, String nom);

    boolean existsByCentreIdAndNomEtablissementIgnoreCaseAndIdNot(Long centreId, String nom, Long id);
}

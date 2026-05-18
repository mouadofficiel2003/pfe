package com.pfe.candidats.repository;

import com.pfe.candidats.domain.Candidat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {

    Optional<Candidat> findByCin(String cin);

    boolean existsByCinAndIdNot(String cin, Long id);

    boolean existsByNumeroInscriptionAndConcoursIdAndIdNot(
            String numeroInscription, Long concoursId, Long id);

    boolean existsByNumeroInscriptionAndConcoursId(String numeroInscription, Long concoursId);
}

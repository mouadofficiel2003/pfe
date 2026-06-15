package com.pfe.candidats.repository;

import com.pfe.candidats.domain.Candidat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatRepository extends JpaRepository<Candidat, String> {

    Optional<Candidat> findByCin(String cin);

    boolean existsByCinAndNumeroInscriptionNot(String cin, String numeroInscription);
}

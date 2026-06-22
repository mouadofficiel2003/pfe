package com.pfe.convocation.repository;

import com.pfe.convocation.domain.ConvocationEnvoi;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationEnvoiRepository extends JpaRepository<ConvocationEnvoi, Long> {

    List<ConvocationEnvoi> findAllByOrderByEnvoyeLeDesc();
}

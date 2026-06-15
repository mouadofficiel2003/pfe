package com.pfe.repartition.repository;

import com.pfe.repartition.domain.RepartitionRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepartitionRunRepository extends JpaRepository<RepartitionRun, Long> {

    List<RepartitionRun> findAllByOrderByDemarreLeDesc();
}

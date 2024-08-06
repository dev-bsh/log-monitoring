package com.log_monitoring.repository;

import com.log_monitoring.model.Condition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionRepository extends JpaRepository<Condition, Long> {
    List<Condition> findAllByAggSettingId(Long id);
    void deleteAllByAggSettingId(Long id);
}

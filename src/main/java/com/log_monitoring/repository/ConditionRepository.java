package com.log_monitoring.repository;

import com.log_monitoring.model.Condition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionRepository extends JpaRepository<Condition, Long> {
    List<Condition> findAllByAggSettingId(Long id);
    void deleteAllByAggSettingId(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Condition c WHERE c.aggSetting.id IN :settingIds")
    void deleteAllByAggSettingIds(List<Long> settingIds);
}

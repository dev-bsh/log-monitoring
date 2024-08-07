package com.log_monitoring.repository;

import com.log_monitoring.model.AggSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggSettingRepository extends JpaRepository<AggSetting, Long> {
    List<AggSetting> findAllByTopicName(String topicName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AggSetting a WHERE a.topicName = :topicName")
    void deleteAllByTopicName(String topicName);
}
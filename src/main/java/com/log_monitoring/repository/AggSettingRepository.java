package com.log_monitoring.repository;

import com.log_monitoring.model.AggSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggSettingRepository extends JpaRepository<AggSetting, Long> {
}
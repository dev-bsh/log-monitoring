package com.log_monitoring.repository.elasticsearch;

import com.log_monitoring.dto.LogDataAggSearchRequest;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.model.elasticsearch.LogData;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface LogDataRepository {
    void save(LogData logData);
    SearchHits<LogData> findAllByCondition(LogDataSearchRequest requestDto);
    SearchHits<LogData> findAllAggByCondition(LogDataAggSearchRequest requestDto);
}

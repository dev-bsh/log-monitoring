package com.log_monitoring.repository.elasticsearch;

import com.log_monitoring.dto.LogDataSearchKeywordRequest;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.model.elasticsearch.LogData;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface LogDataRepositoryExtension {
    SearchHits<LogData> findAllBetweenTime(LogDataSearchRequest requestDto);
    SearchHits<LogData> findAllByKeywordInDataBetweenTime(LogDataSearchKeywordRequest requestDto);

}

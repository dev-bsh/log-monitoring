package com.log_monitoring.service;

import com.log_monitoring.dto.LogDataSearchKeywordRequest;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.dto.LogDataSearchResponse;
import com.log_monitoring.model.elasticsearch.LogData;
import com.log_monitoring.repository.elasticsearch.LogDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogDataService {

    private final LogDataRepository logDataRepository;

    public void save(LogData logData) {
        logDataRepository.save(logData);
    }

    // 로그데이터 검색 (토픽, 시간)
    public LogDataSearchResponse findAllBetweenTime(LogDataSearchRequest requestDto) {
        SearchHits<LogData> result = logDataRepository.findAllBetweenTime(requestDto);
        return LogDataSearchResponse.fromSearchHits(result);
    }

    // 로그데이터 검색 (토픽, 시간, 필드, 키워드)
    public LogDataSearchResponse findAllByKeywordBetweenTime(LogDataSearchKeywordRequest requestDto) {
        SearchHits<LogData> result = logDataRepository.findAllByKeywordInDataBetweenTime(requestDto);
        return LogDataSearchResponse.fromSearchHits(result);
    }


    // 통계 데이터 검색

    // 실시간 통계 데이터 생성 스케줄러

}

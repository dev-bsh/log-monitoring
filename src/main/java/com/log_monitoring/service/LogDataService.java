package com.log_monitoring.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.FilterAggregate;
import com.log_monitoring.dto.LogDataAggSearchRequest;
import com.log_monitoring.dto.LogDataAggSearchResponse;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.dto.LogDataSearchResponse;
import com.log_monitoring.model.elasticsearch.LogData;
import com.log_monitoring.repository.elasticsearch.LogDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogDataService {

    private final LogDataRepository logDataRepository;

    public void save(LogData logData) {
        Map<String, Object> newData = new HashMap<>();
        // 필드이름 중복 피하기 위해 topicName, fieldName 결합
        logData.getData().forEach((key, value) -> {
            newData.put(logData.getTopicName()+"_"+key, value);
        });
        LogData convertedLogData = LogData.builder()
                .topicName(logData.getTopicName())
                .timestamp(logData.getTimestamp())
                .data(newData)
                .build();
        logDataRepository.save(convertedLogData);
    }

    // raw data 조회
    public LogDataSearchResponse findAllByCondition(LogDataSearchRequest requestDto) {
        SearchHits<LogData> result = logDataRepository.findAllByCondition(requestDto);
        return LogDataSearchResponse.fromSearchHits(result);
    }

    // aggregation data 조회
    public LogDataAggSearchResponse findAllAggByCondition(LogDataAggSearchRequest requestDto) {
        long term = requestDto.getTo() - requestDto.getFrom();
        if (term > 86400_000 * 7) {
            throw new IllegalArgumentException("[Timestamp Range ERROR] 조회하려는 시간범위가 7일보다 크게 설정되었습니다.");
        }

        SearchHits<LogData> searchHits = logDataRepository.findAllAggByCondition(requestDto);
        List<LogDataAggSearchResponse.AggResult> resultList = new ArrayList<>();
        // searchHits에서 집계 결과 추출
        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            Map<String, ElasticsearchAggregation> aggregationMap = Objects.requireNonNull(aggregations).aggregationsAsMap();
            for (String settingName : aggregationMap.keySet()) {
                Aggregate aggregate = aggregationMap.get(settingName).aggregation().getAggregate();
                Buckets<DateHistogramBucket> buckets;
                if (aggregate.isDateHistogram()) { // 범위내 모든 로그 대상 DateHistogram
                    buckets = aggregate.dateHistogram().buckets();
                } else { // 설정 Filter 로그 대상 DateHistogram
                    buckets = aggregate.filter().aggregations().get("interval").dateHistogram().buckets();
                }
                // 집계 결과 값에서 timestamp, count 값 매핑
                List<LogDataAggSearchResponse.AggResult.Data> dataList = buckets.array().stream()
                        .map(LogDataAggSearchResponse.AggResult.Data::fromBucket)
                        .toList();
                resultList.add(LogDataAggSearchResponse.AggResult.builder()
                        .settingName(settingName)
                        .data(dataList).build());
            }
        }
        return LogDataAggSearchResponse.builder()
                .topicName(requestDto.getTopicName())
                .result(resultList)
                .build();
    }

}

package com.log_monitoring.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
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
        logDataRepository.save(logData);
    }

    // raw data 조회
    public LogDataSearchResponse findAllByCondition(LogDataSearchRequest requestDto) {
        SearchHits<LogData> result = logDataRepository.findAllByCondition(requestDto);
        return LogDataSearchResponse.fromSearchHits(result);
    }

    // aggregation data 조회
    public LogDataAggSearchResponse findAllAggByCondition(LogDataAggSearchRequest requestDto) {
        if (requestDto.getTo() - requestDto.getFrom() > 86400_000 * 7) {
            throw new IllegalArgumentException("[Timestamp Range ERROR] 조회하려는 시간범위가 7일보다 크게 설정되었습니다.");
        }

        SearchHits<LogData> searchHits = logDataRepository.findAllAggByCondition(requestDto);
        List<LogDataAggSearchResponse.AggResult> resultList = getAggResultFromSearchHits(searchHits);

        return LogDataAggSearchResponse.builder()
                .topicName(requestDto.getTopicName())
                .result(resultList)
                .build();
    }
    
    // searchHits에서 집계 결과 추출
    private List<LogDataAggSearchResponse.AggResult> getAggResultFromSearchHits(SearchHits<LogData> searchHits) {
        List<LogDataAggSearchResponse.AggResult> resultList = new ArrayList<>();

        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            Map<String, ElasticsearchAggregation> aggMap = Objects.requireNonNull(aggregations).aggregationsAsMap();
            for (String settingName : aggMap.keySet()) {
                Aggregate aggregate = aggMap.get(settingName).aggregation().getAggregate();
                Buckets<DateHistogramBucket> buckets;
                if (aggregate.isDateHistogram()) { // Filter 없는 DateHistogram
                    buckets = aggregate.dateHistogram().buckets();
                } else { // Filter 집계 DateHistogram
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

        return resultList;
    }

}

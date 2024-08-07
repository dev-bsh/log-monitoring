package com.log_monitoring.service;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        SearchHits<LogData> searchHits = logDataRepository.findAllAggByCondition(requestDto);
        List<LogDataAggSearchResponse.AggResult> resultList = new ArrayList<>();
        // searchHits에서 집계 결과 추출
        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            Map<String, ElasticsearchAggregation> aggregationMap = Objects.requireNonNull(aggregations).aggregationsAsMap();
            for (String settingName : aggregationMap.keySet()) {
                Buckets<DateHistogramBucket> buckets;
                if (settingName.equals("total_logs")) { // 전체 로그 집계 결과
                    buckets = aggregationMap.get("total_logs").aggregation().getAggregate().dateHistogram().buckets();
                } else { //설정별 로그 집계 결과
                    FilterAggregate filterAggregate = aggregationMap.get(settingName).aggregation().getAggregate().filter();
                    buckets = filterAggregate.aggregations().get("interval").dateHistogram().buckets();
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

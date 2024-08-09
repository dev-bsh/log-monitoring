package com.log_monitoring.repository.elasticsearch;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.FieldDateMath;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import com.log_monitoring.dto.ConditionDto;
import com.log_monitoring.dto.LogDataAggSearchRequest;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.model.elasticsearch.LogData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogDataRepositoryImpl implements LogDataRepository {

    private final ElasticsearchOperations elasticsearchOperations;
    private final String TOPIC_NAME = "topicName";
    private final String TIMESTAMP = "timestamp";

    @Override
    public void save(LogData logData) {
        IndexCoordinates index = getIndexCoordinates(logData.getTopicName());
        elasticsearchOperations.save(logData, index);
    }

    @Override
    public SearchHits<LogData> findAllByCondition(LogDataSearchRequest request) {
        IndexCoordinates index = getIndexCoordinates(request.getTopicName());
        Query query = NativeQuery.builder()
                .withQuery(q -> q.bool(bool -> {
                    bool.filter(f -> f.term(t -> t.field(TOPIC_NAME).value(request.getTopicName())))
                        .filter(f -> f.range(r -> r.field(TIMESTAMP).gte(JsonData.of(request.getFrom())).lte(JsonData.of(request.getTo()))));
                        addConditionQuery(bool, request.getCondition(), request.getTopicName());
                    return bool;
                }))
                .build();
        return elasticsearchOperations.search(query, LogData.class, index);
    }

    @Override
    public SearchHits<LogData> findAllAggByCondition(LogDataAggSearchRequest request) {
        IndexCoordinates index = getIndexCoordinates(request.getTopicName());
        boolean isContainTotalSearch = request.getSearchSettings().stream().anyMatch(setting -> setting.getConditionList().isEmpty());
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(bool -> {
                    bool.filter(f -> f.term(t -> t.field(TOPIC_NAME).value(request.getTopicName())))
                        .filter(f -> f.range(r -> r.field(TIMESTAMP).gte(JsonData.of(request.getFrom())).lte(JsonData.of(request.getTo()))));
                    // 전체 조회 설정이 없으면 should 쿼리로 집계 전 범위 좁히기
                    if (!isContainTotalSearch) {
                        bool.should(s -> s.bool(b -> {
                            request.getSearchSettings().forEach(searchSetting ->
                                    addConditionQuery(b, searchSetting.getConditionList(), request.getTopicName()));
                            return b;
                        }));
                    }
                    return bool;
                }));
        // 집계 쿼리 추가
        for (LogDataAggSearchRequest.SearchSetting setting : request.getSearchSettings()) {
            if (setting.getConditionList().isEmpty()) { // filter 조건 없는 설정이면 전체 로그 집계
                queryBuilder.withAggregation(setting.getSettingName(), addDateHistogramAggregation(request));
            } else { // filter 조건으로 집계
                queryBuilder.withAggregation(setting.getSettingName(), addAggregationWithConditionQuery(setting.getConditionList(), request));
            }
        }
        Query query = queryBuilder.build();
        return elasticsearchOperations.search(query, LogData.class, index);
    }

    // 조건별 DateHistogram
    private Aggregation addAggregationWithConditionQuery(List<ConditionDto> conditionList, LogDataAggSearchRequest request) {
        return Aggregation.of(agg -> agg
                .filter(q -> q.bool(b -> {
                    addConditionQuery(b, conditionList, request.getTopicName());
                    return b;
                }))
                .aggregations("interval", addDateHistogramAggregation(request)));
    }

    private Aggregation addDateHistogramAggregation(LogDataAggSearchRequest request) {
        return Aggregation.of(agg -> agg
                .dateHistogram(dh -> dh.field(TIMESTAMP)
                        .calendarInterval(getInterval(request))
                        .minDocCount(0)
                        .extendedBounds(eb -> eb
                                .min(FieldDateMath.of(f -> f.value(request.getFrom().doubleValue())))
                                .max(FieldDateMath.of(f -> f.value(request.getTo().doubleValue())))
                        )
                ));
    }

    private void addConditionQuery(BoolQuery.Builder bool, List<ConditionDto> conditionList, String topicName) {
        for (ConditionDto condition: conditionList) {
            String fieldName = "data."+condition.getFieldName();
            if (condition.getEqual()) {
                // 완전 일치 시 keyword 값으로 탐색
                bool.filter(f -> f.term(t -> t.field(fieldName+".keyword").value(condition.getKeyword())));
            } else {
                bool.filter(f -> f.match(t -> t.field(fieldName).query(condition.getKeyword())));
            }
        }
    }

    private CalendarInterval getInterval(LogDataAggSearchRequest request) {
        long diff = request.getTo() - request.getFrom();
        if (diff <= 3600_000) {
            return  CalendarInterval.Minute;
        } else if (diff <= 86400_000) {
            return CalendarInterval.Hour;
        } else {
            return CalendarInterval.Day;
        }
    }

    private IndexCoordinates getIndexCoordinates(String topicName) {
        String indexName = "index_" + topicName;
        return IndexCoordinates.of(indexName.toLowerCase());
    }

}

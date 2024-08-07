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
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogDataRepositoryExtensionImpl implements LogDataRepositoryExtension {

    private final ElasticsearchOperations elasticsearchOperations;
    private final IndexCoordinates index = IndexCoordinates.of("logs");
    private final String TOPIC_NAME = "topicName";
    private final String TIMESTAMP = "timestamp";
    private final String DATA = "data.";

    @Override
    public SearchHits<LogData> findAllByCondition(LogDataSearchRequest request) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.bool(bool -> {
                    bool.filter(f -> f.term(t -> t.field(TOPIC_NAME).value(request.getTopicName())))
                        .filter(f -> f.range(r -> r.field(TIMESTAMP).gte(JsonData.of(request.getFrom())).lte(JsonData.of(request.getTo()))));
                        addConditionQuery(bool, request.getCondition());
                    return bool;
                }))
                .build();
        return elasticsearchOperations.search(query, LogData.class, index);
    }

    @Override
    public SearchHits<LogData> findAllAggByCondition(LogDataAggSearchRequest request) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(bool -> bool
                        .filter(f -> f.term(t -> t.field(TOPIC_NAME).value(request.getTopicName())))
                        .filter(f -> f.range(r -> r.field(TIMESTAMP).gte(JsonData.of(request.getFrom())).lte(JsonData.of(request.getTo())))))
                );
        // 집계 조건 추가
        for (LogDataAggSearchRequest.SearchSetting setting : request.getSearchSettings()) {
            if (!setting.getConditionList().isEmpty()) {
                addAggregationWithConditionQuery(queryBuilder, setting, request);
            } else {
                addAggregationTotalQuery(queryBuilder, setting.getSettingName() , request);
            }
        }
        Query query = queryBuilder.build();
        return elasticsearchOperations.search(query, LogData.class, index);
    }

    // 조건별 DateHistogram
    private void addAggregationWithConditionQuery(NativeQueryBuilder queryBuilder, LogDataAggSearchRequest.SearchSetting setting, LogDataAggSearchRequest request) {
        queryBuilder.withAggregation(setting.getSettingName(), Aggregation.of(agg -> agg
                .filter(q -> q.bool(b -> {
                    addConditionQuery(b, setting.getConditionList());
                    return b;
                }))
                .aggregations("interval", Aggregation.of(subAgg -> subAgg
                        .dateHistogram(dh -> dh.field(TIMESTAMP)
                                .calendarInterval(getInterval(request))
                                .minDocCount(0)
                                .extendedBounds(eb -> eb
                                        .min(FieldDateMath.of(f -> f.value(request.getFrom().doubleValue())))
                                        .max(FieldDateMath.of(f -> f.value(request.getTo().doubleValue())))
                                )
                        ))
                ))
        );
    }

    // 전체 데이터 DateHistogram
    private void addAggregationTotalQuery(NativeQueryBuilder queryBuilder, String settingName, LogDataAggSearchRequest request) {
        queryBuilder.withAggregation(settingName, Aggregation.of(agg -> agg
                .dateHistogram(dh -> dh.field(TIMESTAMP)
                        .calendarInterval(getInterval(request))
                        .minDocCount(0)
                        .extendedBounds(eb -> eb
                                .min(FieldDateMath.of(f -> f.value(request.getFrom().doubleValue())))
                                .max(FieldDateMath.of(f -> f.value(request.getTo().doubleValue())))
                        )
                ))
        );
    }

    private void addConditionQuery(BoolQuery.Builder bool, List<ConditionDto> conditionList) {
        for (ConditionDto condition: conditionList) {
            String fieldName = DATA+condition.getFieldName();
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

}

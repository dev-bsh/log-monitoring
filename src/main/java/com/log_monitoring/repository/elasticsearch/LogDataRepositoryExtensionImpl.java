package com.log_monitoring.repository.elasticsearch;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import com.log_monitoring.dto.ConditionDto;
import com.log_monitoring.dto.LogDataAggSearchRequest;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.model.elasticsearch.LogData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
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
                        .filter(f -> f.range(r -> r.field(TIMESTAMP).gte(JsonData.of(request.getFrom())).lte(JsonData.of(request.getTo()))))))
                .withAggregation("total_logs", Aggregation.of(agg ->
                        agg.dateHistogram(dh -> dh.field(TIMESTAMP).calendarInterval(getInterval(request)))));

        // 집계 조건 추가
        for (LogDataAggSearchRequest.SearchSetting setting : request.getSearchSettings()) {
            queryBuilder.withAggregation(setting.getSettingName(), Aggregation.of(agg ->
                    agg.filter(q -> q.bool(b -> {
                        addConditionQuery(b, setting.getConditionList());
                        return b;
                    })).aggregations("interval", Aggregation.of(subAgg ->
                            subAgg.dateHistogram(dh -> dh.field(TIMESTAMP).calendarInterval(getInterval(request)))))
            ));
        }

        Query query = queryBuilder.build();
        return elasticsearchOperations.search(query, LogData.class, index);
    }



    private void addConditionQuery(BoolQuery.Builder bool, List<ConditionDto> conditionList) {
        for (ConditionDto condition: conditionList) {
            String fieldName = DATA+condition.getFieldName();
            if (condition.getEqual()) {
                bool.filter(f -> f.term(t -> t.field(fieldName).value(condition.getKeyword())));
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

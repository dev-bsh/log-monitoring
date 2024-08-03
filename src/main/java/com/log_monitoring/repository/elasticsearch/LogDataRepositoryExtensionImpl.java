package com.log_monitoring.repository.elasticsearch;

import co.elastic.clients.json.JsonData;
import com.log_monitoring.dto.LogDataSearchKeywordRequest;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.model.elasticsearch.LogData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogDataRepositoryExtensionImpl implements LogDataRepositoryExtension {

    private final ElasticsearchOperations elasticsearchOperations;
    private final IndexCoordinates index = IndexCoordinates.of("logs");
    private final String TOPIC_NAME = "topicName";
    private final String TIMESTAMP = "timestamp";
    private final String DATA = "data.";

    @Override
    public SearchHits<LogData> findAllBetweenTime(LogDataSearchRequest request) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .filter(m -> m.term(t -> t.field(TOPIC_NAME).value(request.getTopicName())))
                                .filter(m -> m.range(r -> r.field(TIMESTAMP).gte(JsonData.of(request.getFrom())).lte(JsonData.of(request.getTo()))))
                        ))
                .build();
        return elasticsearchOperations.search(query, LogData.class, index);
    }

    @Override
    public SearchHits<LogData> findAllByKeywordInDataBetweenTime(LogDataSearchKeywordRequest request) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .filter(m -> m.term(t -> t.field(TOPIC_NAME).value(request.getTopicName())))
                                .filter(m -> m.range(r -> r.field(TIMESTAMP).gte(JsonData.of(request.getFrom())).lte(JsonData.of(request.getTo()))))
                                .filter(f -> f.match(mc -> mc.field(DATA+request.getFieldName()).query(request.getKeyword())))
                        ))
                .build();
        return elasticsearchOperations.search(query, LogData.class, index);
    }

    // 통계 데이터 조회 전체

    // 통계 데이터 조회 조건
}

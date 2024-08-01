package com.log_monitoring.model.elasticsearch;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@Document(indexName = "log_data")
@Getter
@NoArgsConstructor
public class LogData {

    @Id
    private String id;
    private String topicName;
    private Map<String, Object> data;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long timestamp;

    @Builder
    public LogData(String id, String topicName, Map<String, Object> data, Long timestamp) {
        this.id = id;
        this.topicName = topicName;
        this.data = data;
        this.timestamp = timestamp;
    }

}

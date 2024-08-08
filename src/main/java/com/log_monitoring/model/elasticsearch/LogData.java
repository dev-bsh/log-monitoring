package com.log_monitoring.model.elasticsearch;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Map;

@Document(indexName = "logs")
@Setting(settingPath = "es-setting.json")
@Mapping(mappingPath = "es-mapping.json")
@Getter
@NoArgsConstructor
public class LogData {

    @Id
    private String id;
    private String topicName;
    private Map<String, Object> data;
    private Long timestamp;

    @Builder
    public LogData(String id, String topicName, Map<String, Object> data, Long timestamp) {
        this.id = id;
        this.topicName = topicName;
        this.data = data;
        this.timestamp = timestamp;
    }

}

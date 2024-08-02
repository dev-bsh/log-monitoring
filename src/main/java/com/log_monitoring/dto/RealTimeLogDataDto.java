package com.log_monitoring.dto;

import com.log_monitoring.model.elasticsearch.LogData;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RealTimeLogDataDto {

    private String topicName;
    private Long timestamp;
    private Map<String, Object> data;

    @Builder
    public RealTimeLogDataDto(String topicName, Long timestamp, Map<String, Object> data) {
        this.topicName = topicName;
        this.timestamp = timestamp;
        this.data = data;
    }

    public static RealTimeLogDataDto fromEntity(LogData logData) {
        return RealTimeLogDataDto.builder()
                .topicName(logData.getTopicName())
                .timestamp(logData.getTimestamp())
                .data(logData.getData())
                .build();
    }
}

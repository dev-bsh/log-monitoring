package com.log_monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log_monitoring.config.InMemoryTopicMetadata;
import com.log_monitoring.config.WebSocketHandler;
import com.log_monitoring.dto.FieldDto;
import com.log_monitoring.dto.LogDataDto;
import com.log_monitoring.dto.TopicDto;
import com.log_monitoring.model.elasticsearch.LogData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessingService {

    private final InMemoryTopicMetadata inMemoryTopicMetadata;
    private final WebSocketHandler webSocketHandler;
    private final LogDataService logDataService;
    private final ObjectMapper objectMapper;

    public void processMessage(ConsumerRecord<String, String> record) {
        TopicDto topicDto = inMemoryTopicMetadata.getTopicMetadata(record.topic());
        LogData logData = convertMessageToLogData(record.value(), topicDto).orElseThrow(IllegalArgumentException::new);
        LogDataDto logDataDto = LogDataDto.fromEntity(logData);
        // 엘라스틱 서치 저장
        logDataService.save(logData);
        // 프론트엔드 전송
        webSocketHandler.sendMessageToClients(logDataDto);
    }

    private Optional<LogData> convertMessageToLogData(String message, TopicDto topicDto) {
        try {
            String TIME_STAMP = "timestamp";
            Map<String, Object> rawData = objectMapper.readValue(message, new TypeReference<>() {});


            Long timestamp = System.currentTimeMillis();
            if (rawData.containsKey(TIME_STAMP)) {
                if (rawData.get(TIME_STAMP) instanceof Long) {
                    timestamp = (Long) rawData.get(TIME_STAMP);
                    rawData.remove(TIME_STAMP);
                } else {
                    throw new IllegalArgumentException("[필드 오류] timestamp type error");
                }
            }

            Map<String, String> metaData = topicDto.getFields().stream()
                    .collect(Collectors.toMap(FieldDto::getFieldName, FieldDto::getFieldType));

            // metaData rawData 비교
            for (String rawFieldName : metaData.keySet()) {
                if (!metaData.containsKey(rawFieldName)) {
                    log.error("[필드 매핑 오류] 메타데이터에 없는 필드입니다. FieldName: {}", rawFieldName);
                    throw new IllegalArgumentException("[필드 오류] 메타데이터에 없는 필드입니다.");
                } else if (!isValidType(metaData.get(rawFieldName), rawData.get(rawFieldName))) {
                    log.error("[필드 매핑 오류] 메타데이터에 정의된 타입과 다른 데이터입니다. FieldName: {}, data: {}", rawFieldName, rawData.get(rawFieldName));
                    throw new IllegalArgumentException("[필드 오류] 메타데이터 설정과 다른 타입의 필드입니다.");
                }
            }

            return Optional.of(LogData.builder()
                    .topicName(topicDto.getTopicName())
                    .timestamp(timestamp)
                    .data(rawData)
                    .build());
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // 타입 확인
    private boolean isValidType(String metadataType, Object value) {
        switch (metadataType.toLowerCase()) {
            case "int":
                if (value instanceof Integer) return true;
                if (value instanceof Number) {
                    return ((Number) value).intValue() == ((Number) value).doubleValue();
                }
                return false;
            case "long":
                if (value instanceof Long) return true;
                if (value instanceof Number) {
                    return ((Number) value).longValue() == ((Number) value).doubleValue();
                }
                return false;
            case "float":
                return value instanceof Float || value instanceof Double;
            case "double":
                return value instanceof Double;
            case "string":
                return value instanceof String;
            case "boolean":
                return value instanceof Boolean;
            case "json":
                return value instanceof Map;
            case "list":
                return value instanceof List;
            default:
                return false;
        }
    }
}

package com.log_monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log_monitoring.config.InMemoryTopicMetadata;
import com.log_monitoring.dto.FieldDto;
import com.log_monitoring.dto.LogDataDto;
import com.log_monitoring.dto.TopicDto;
import com.log_monitoring.model.elasticsearch.LogData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final LogDataService logDataService;
    private final ObjectMapper objectMapper;

    public void processMessage(ConsumerRecord<String, String> record) {
        try {
            TopicDto topicDto = inMemoryTopicMetadata.getTopicMetadata(record.topic());
            LogData logData = convertMessageToLogData(record.value(), topicDto).orElseThrow(IllegalArgumentException::new);
            LogDataDto logDataDto = LogDataDto.fromEntity(logData);
            // 엘라스틱 서치 저장
            logDataService.save(logData);
            // 프론트엔드 전송
            simpMessagingTemplate.convertAndSend("/topic/raw/"+logDataDto.getTopicName(), logDataDto);
        } catch (Exception e) {
            log.error("kafka listener message process error: {}", e.getMessage());
        }

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
                    throw new IllegalArgumentException("[FIELD ERROR] timestamp Type Error");
                }
            }
            Map<String, String> metaData = topicDto.getFields().stream()
                    .collect(Collectors.toMap(FieldDto::getFieldName, FieldDto::getFieldType));
            // metaData rawData 비교
            for (String rawFieldName : rawData.keySet()) {
                if (!metaData.containsKey(rawFieldName)) {
                    log.error("[FIELD ERROR] 메타데이터에 없는 필드입니다. Input Data Field Name: {}", rawFieldName);
                    throw new IllegalArgumentException("[FIELD ERROR] 메타데이터에 없는 필드입니다.");
                } else if (!isValidType(metaData.get(rawFieldName), rawData.get(rawFieldName))) {
                    log.error("[FIELD ERROR] 메타데이터에 설정된 타입과 다른 데이터입니다. [ 필드 이름: {} | 설정 타입: {} | Input Data: {} | Input Data Type: {} ] ",
                            rawFieldName, metaData.get(rawFieldName), rawData.get(rawFieldName), rawData.get(rawFieldName).getClass().getName());
                    throw new IllegalArgumentException("[FIELD ERROR] 메타데이터 설정과 다른 타입의 필드입니다.");
                }
            }
            return Optional.of(LogData.builder()
                    .topicName(topicDto.getTopicName())
                    .timestamp(timestamp)
                    .data(rawData)
                    .build());
        } catch (JsonProcessingException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    // 타입 확인
    private boolean isValidType(String metadataType, Object value) {
        if (value == null) return false;
        return switch (metadataType.toLowerCase()) {
            case "integer" -> value instanceof Integer;
            case "long" -> value instanceof Long || value instanceof Integer;
            case "float" -> value instanceof Float;
            case "double" -> value instanceof Double || value instanceof Float;
            case "string" -> value instanceof String;
            case "boolean" -> value instanceof Boolean;
            case "json" -> value instanceof Map;
            case "list" -> value instanceof List;
            case "object" -> true;
            default -> false;
        };
    }

}

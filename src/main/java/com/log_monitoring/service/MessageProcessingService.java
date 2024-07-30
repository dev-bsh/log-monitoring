package com.log_monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log_monitoring.config.InMemoryTopicMetadata;
import com.log_monitoring.dto.TopicDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessingService {

    private InMemoryTopicMetadata inMemoryTopicMetadata;

    public void processMessage(ConsumerRecord<String, String> record) {
        TopicDto topicDto = inMemoryTopicMetadata.getTopicMetadata(record.topic());
        String message =  record.value();
        // 데이터 검증

        // Elasticsearch 저장

        // redis 저장
    }
}

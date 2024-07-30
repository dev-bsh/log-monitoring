package com.log_monitoring.config;

import com.log_monitoring.dto.FieldDto;
import com.log_monitoring.dto.TopicDto;
import com.log_monitoring.model.FieldMetadata;
import com.log_monitoring.model.TopicMetadata;
import com.log_monitoring.repository.FieldMetadataRepository;
import com.log_monitoring.repository.TopicMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryTopicMetadata {

    private final Map<String, TopicDto> topicMetadataCache = new ConcurrentHashMap<>();
    private final TopicMetadataRepository topicMetadataRepository;
    private final FieldMetadataRepository fieldMetadataRepository;

    @PostConstruct
    public void init() {
        topicMetadataRepository.findAll().stream()
                .map(topicEntity -> TopicDto.fromEntity(topicEntity, fieldMetadataRepository.findAllByTopicMetadataId(topicEntity.getId())))
                .forEach(topicDto -> topicMetadataCache.put(topicDto.getTopicName(), topicDto));
        log.info("Topic metadata loaded successfully : {}", topicMetadataCache);
    }

    public void addTopicMetadata(TopicMetadata savedTopicMetadata, Set<FieldMetadata> fieldSet) {
        TopicDto topicDto = TopicDto.fromEntity(savedTopicMetadata, fieldSet);
        topicMetadataCache.put(topicDto.getTopicName(), topicDto);
    }

    public void removeTopicMetadata(String topicName) {
        topicMetadataCache.remove(topicName);
    }

    public void updateTopicMetadata(TopicMetadata topicMetadata, Set<FieldDto> newFields) {
        TopicDto topicDto = TopicDto.fromEntity(topicMetadata, newFields.stream().map(fid -> fid.toEntity(topicMetadata)).collect(Collectors.toSet()));
        topicMetadataCache.replace(topicDto.getTopicName(), topicDto);
    }

    public TopicDto getTopicMetadata(String topicName) {
        return topicMetadataCache.get(topicName);
    }

    public List<TopicDto> getAllTopics() {
        return new ArrayList<>(topicMetadataCache.values());
    }
}

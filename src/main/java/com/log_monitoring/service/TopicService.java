package com.log_monitoring.service;

import com.log_monitoring.config.InMemoryTopicMetadata;
import com.log_monitoring.dto.FieldDto;
import com.log_monitoring.dto.TopicDto;
import com.log_monitoring.dto.TopicSaveRequest;
import com.log_monitoring.dto.TopicUpdateRequest;
import com.log_monitoring.model.FieldMetadata;
import com.log_monitoring.model.TopicMetadata;
import com.log_monitoring.repository.FieldMetadataRepository;
import com.log_monitoring.repository.TopicMetadataRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicMetadataRepository topicMetadataRepository;
    private final FieldMetadataRepository fieldMetadataRepository;
    private final AggSettingService aggSettingService;
    private final InMemoryTopicMetadata inMemoryTopicMetadata;
    private final KafkaService kafkaService;
    private final IndexService indexService;

    @Transactional(rollbackOn = {ExecutionException.class, InterruptedException.class, RuntimeException.class})
    public Long createTopic(TopicSaveRequest topicRequest) throws ExecutionException, InterruptedException {
        if (topicMetadataRepository.findByTopicName(topicRequest.getTopicName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 토픽입니다. topicName: " +topicRequest.getTopicName());
        }
        // DB 저장
        TopicMetadata savedTopicMetadata = topicMetadataRepository.save(topicRequest.toEntity());
        Set<FieldMetadata> fieldSet = topicRequest.getFields().stream()
                .map(fieldDto -> fieldDto.toEntity(savedTopicMetadata))
                .collect(Collectors.toSet());
        fieldMetadataRepository.saveAll(fieldSet);

        kafkaService.createTopic(topicRequest); // 카프카 토픽 생성
        indexService.createIndex(topicRequest.getTopicName()); // ES Index 생성
        inMemoryTopicMetadata.addTopicMetadata(savedTopicMetadata, fieldSet); // 캐시 저장
        return savedTopicMetadata.getId();
    }

    @Transactional(rollbackOn = {ExecutionException.class, InterruptedException.class, RuntimeException.class})
    public void deleteTopic(Long id) throws ExecutionException, InterruptedException {
        TopicMetadata topicMetadata = topicMetadataRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 topic 입니다. id: " + id));
        String topicName = topicMetadata.getTopicName();
        // DB 삭제
        fieldMetadataRepository.deleteByTopicMetadataId(topicMetadata.getId());
        topicMetadataRepository.delete(topicMetadata);

        aggSettingService.deleteAllByTopicName(topicName); // 실시간 집계 설정 제거
        kafkaService.deleteTopic(topicName); // 카프카 토픽 제거
        indexService.deleteIndex(topicName); // ES Index 제거
        inMemoryTopicMetadata.removeTopicMetadata(topicName); // 캐시 제거
    }

    // 토픽 DB, cache 설정 변경
    @Transactional
    public TopicDto updateTopic(TopicUpdateRequest updateRequest) {
        // 토픽 description 수정
        TopicMetadata topicMetadata = topicMetadataRepository.findById(updateRequest.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 topic 입니다. id: " + updateRequest.getId()));
        topicMetadata.updateDescription(updateRequest.getTopicDescription());
        topicMetadataRepository.save(topicMetadata);
        // 기존 필드와의 중복 체크 후 저장
        Set<FieldMetadata> newFields = compareFields(topicMetadata, updateRequest.getFields());
        fieldMetadataRepository.saveAll(newFields);
        // 캐시 업데이트
        inMemoryTopicMetadata.updateTopicMetadata(topicMetadata, newFields);
        return inMemoryTopicMetadata.getTopicMetadata(topicMetadata.getTopicName());
    }

    public List<TopicDto> findAllTopics() {
        inMemoryTopicMetadata.refreshTopicMetadata();
        return inMemoryTopicMetadata.getAllTopics();
    }

    public Set<FieldMetadata> compareFields(TopicMetadata topicMetadata, Set<FieldDto> newFieldSet) {
        Set<FieldMetadata> existingField = fieldMetadataRepository.findAllByTopicMetadataId(topicMetadata.getId());
        existingField.stream().map(FieldDto::fromEntity).forEach(newFieldSet::remove);
        return newFieldSet.stream().map(fieldDto -> fieldDto.toEntity(topicMetadata)).collect(Collectors.toSet());
    }
}

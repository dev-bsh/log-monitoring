package com.log_monitoring.service;

import com.log_monitoring.config.DynamicKafkaListenerManager;
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
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final AdminClient adminClient;
    private final TopicMetadataRepository topicMetadataRepository;
    private final FieldMetadataRepository fieldMetadataRepository;
    private final InMemoryTopicMetadata inMemoryTopicMetadata;
    private final DynamicKafkaListenerManager listenerManager;

    @Transactional(rollbackOn = {ExecutionException.class, InterruptedException.class, RuntimeException.class})
    public Long createTopic(TopicSaveRequest topicRequest) throws ExecutionException, InterruptedException {
        // DB 및 캐시 저장
        TopicMetadata savedTopicMetadata = topicMetadataRepository.save(topicRequest.toEntity());
        Set<FieldMetadata> fieldSet = topicRequest.getFields().stream()
                .map(fieldDto -> fieldDto.toEntity(savedTopicMetadata))
                .collect(Collectors.toSet());
        fieldMetadataRepository.saveAll(fieldSet);
        inMemoryTopicMetadata.addTopicMetadata(savedTopicMetadata, fieldSet);
        // 카프카 토픽 생성
        NewTopic newTopic = new NewTopic(topicRequest.getTopicName(), topicRequest.getPartitions(), (short) topicRequest.getReplicationFactor());
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        // 리스너 생성
        listenerManager.addListener(savedTopicMetadata.getTopicName());
        return savedTopicMetadata.getId();
    }

    @Transactional(rollbackOn = {ExecutionException.class, InterruptedException.class, RuntimeException.class})
    public void deleteTopic(Long id) throws ExecutionException, InterruptedException {
        TopicMetadata topicMetadata = topicMetadataRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 topic 입니다. id: " + id));
        // 리스너 제거
        listenerManager.removeListener(topicMetadata.getTopicName());
        // 카프카 토픽 제거
        adminClient.deleteTopics(Collections.singleton(topicMetadata.getTopicName())).all().get();
        // DB 및 캐시에서 삭제
        inMemoryTopicMetadata.removeTopicMetadata(topicMetadata.getTopicName());
        fieldMetadataRepository.deleteByTopicMetadataId(topicMetadata.getId());
        topicMetadataRepository.delete(topicMetadata);
    }

    // 토픽 DB, cache 설정 변경
    @Transactional
    public TopicDto updateTopic(TopicUpdateRequest updateRequest) {
        // 토픽 description 수정
        TopicMetadata topicMetadata = topicMetadataRepository.findById(updateRequest.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 topic 입니다. id: " + updateRequest.getId()));
        topicMetadata.updateDescription(updateRequest.getTopicDescription());
        topicMetadataRepository.save(topicMetadata);
        // 기존 필드와 비교
        Set<FieldDto> newFields = new HashSet<>(updateRequest.getFields());
        Set<Long> removedTargetIds = new HashSet<>();
        Set<FieldMetadata> updatedFields = compareFields(topicMetadata, newFields, removedTargetIds);
        // 비교 완료된 메타데이터 저장
        fieldMetadataRepository.saveAll(updatedFields);
        fieldMetadataRepository.deleteAllByIds(removedTargetIds);
        // 캐시 업데이트
        inMemoryTopicMetadata.updateTopicMetadata(topicMetadata, updateRequest.getFields());
        return inMemoryTopicMetadata.getTopicMetadata(topicMetadata.getTopicName());
    }

    public List<TopicDto> findAllTopics() {
        return inMemoryTopicMetadata.getAllTopics();
    }

    public Set<FieldMetadata> compareFields(TopicMetadata topicMetadata, Set<FieldDto> newFieldSet, Set<Long> removedTargetIds) {
        Set<FieldMetadata> existingField = fieldMetadataRepository.findAllByTopicMetadataId(topicMetadata.getId());
        for (FieldMetadata fieldEntity: existingField) {
            FieldDto existField = FieldDto.fromEntity(fieldEntity);
            if (newFieldSet.contains(existField)) { // 기존과 동일한 필드가 있으면 수정목록에서 제거
                newFieldSet.remove(existField);
            } else { // 사용하지 않는 필드 제거
                removedTargetIds.add(fieldEntity.getId());
            }
        }
        return newFieldSet.stream().map(fieldDto -> fieldDto.toEntity(topicMetadata)).collect(Collectors.toSet());
    }
}

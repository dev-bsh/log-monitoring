package com.log_monitoring.service;

import com.log_monitoring.dto.FieldDto;
import com.log_monitoring.dto.TopicDto;
import com.log_monitoring.dto.TopicSaveRequest;
import com.log_monitoring.dto.TopicUpdateRequest;
import com.log_monitoring.model.FieldMetadata;
import com.log_monitoring.model.TopicMetadata;
import com.log_monitoring.repository.FieldMetadataRepository;
import com.log_monitoring.repository.TopicMetadataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final AdminClient adminClient;
    private final TopicMetadataRepository topicMetadataRepository;
    private final FieldMetadataRepository fieldMetadataRepository;
    private Map<String, TopicDto> topicMetadataCache;

    // 최초 실행 시 DB에 있는 토픽 메타데이터 메모리에 저장
    @PostConstruct
    public void init() {
        topicMetadataCache = new ConcurrentHashMap<>();
        topicMetadataRepository.findAll().stream()
                .map(topicEntity -> TopicDto.fromEntity(topicEntity, fieldMetadataRepository.findAllByTopicMetadataId(topicEntity.getId())))
                .forEach(topicDto -> topicMetadataCache.put(topicDto.getTopicName(), topicDto));
        log.info("Topic metadata loaded successfully : {}", topicMetadataCache.toString());
    }

    @Transactional(rollbackOn = {ExecutionException.class, InterruptedException.class, RuntimeException.class})
    public Long createTopic(TopicSaveRequest topicRequest) throws ExecutionException, InterruptedException {
        // DB 저장
        TopicMetadata savedTopicMetadata = topicMetadataRepository.save(topicRequest.toEntity());
        // field dto -> 엔티티로 변경
        Set<FieldMetadata> fieldSet = topicRequest.getFields().stream()
                .map(fieldDto -> fieldDto.toEntity(savedTopicMetadata))
                .collect(Collectors.toSet());
        fieldMetadataRepository.saveAll(fieldSet);
        // 캐시 저장
        TopicDto topicDto = TopicDto.fromEntity(savedTopicMetadata, fieldSet);
        topicMetadataCache.put(topicDto.getTopicName(), topicDto);
        // 카프카 토픽 생성
        NewTopic newTopic = new NewTopic(topicRequest.getTopicName(), topicRequest.getPartitions(), (short) topicRequest.getReplicationFactor());
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();

        return savedTopicMetadata.getId();
    }

    @Transactional(rollbackOn = {ExecutionException.class, InterruptedException.class, RuntimeException.class})
    public void deleteTopic(Long id) throws ExecutionException, InterruptedException {
        // DB 삭제
        TopicMetadata topicMetadata = topicMetadataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 topic 입니다. id: " + id));
        fieldMetadataRepository.deleteByTopicMetadataId(topicMetadata.getId());
        topicMetadataRepository.delete(topicMetadata);
        // 캐시 삭제
        topicMetadataCache.remove(topicMetadata.getTopicName());
        // 카프카에서 삭제
        adminClient.deleteTopics(Collections.singleton(topicMetadata.getTopicName())).all().get();
    }

    // 토픽 DB, cache 설정 변경
    @Transactional
    public TopicDto updateTopic(TopicUpdateRequest updateRequest) {
        TopicMetadata topicMetadata = topicMetadataRepository.findById(updateRequest.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 topic 입니다. id: " + updateRequest.getId()));
        Set<FieldDto> newFieldSet = new HashSet<>(updateRequest.getFields());
        Set<Long> removedTargetIds = new HashSet<>();
        // 기존 필드와 비교작업 진행
        fieldMetadataRepository.findAllByTopicMetadataId(topicMetadata.getId())
                .forEach(fieldEntity -> {
                    FieldDto existField = FieldDto.fromEntity(fieldEntity);
                    if (newFieldSet.contains(existField)) { // 동일한 필드가 있으면 추가 목록에서 제거
                        newFieldSet.remove(existField);
                    } else { // 사용하지 않는 필드 제거
                        removedTargetIds.add(fieldEntity.getId());
                    }
                });
        // 새로 추가된 필드 저장
        Set<FieldMetadata> updatedFieldSet = newFieldSet.stream().map(fieldDto -> fieldDto.toEntity(topicMetadata)).collect(Collectors.toSet());
        fieldMetadataRepository.saveAll(updatedFieldSet);
        // 사용하지 않는 필드 제거
        fieldMetadataRepository.deleteAllByIds(removedTargetIds);
        // 토픽 설명 수정
        topicMetadata.updateDescription(updateRequest.getTopicDescription());
        topicMetadataRepository.save(topicMetadata);

        // cache update
        TopicDto topicDto = TopicDto.fromEntity(topicMetadata, updateRequest.getFields().stream().map(fid -> fid.toEntity(topicMetadata)).collect(Collectors.toSet()));
        topicMetadataCache.replace(topicDto.getTopicName(), topicDto);
        return topicDto;
    }

    public List<TopicDto> findAllTopics() {
        return new ArrayList<>(topicMetadataCache.values());
    }

    public TopicDto getTopicMetadata(String topicName) {
        return topicMetadataCache.get(topicName);
    }

}

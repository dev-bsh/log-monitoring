package com.log_monitoring.service;

import com.log_monitoring.dto.AggSettingResponse;
import com.log_monitoring.dto.AggSettingSaveRequest;
import com.log_monitoring.dto.ConditionDto;
import com.log_monitoring.model.AggSetting;
import com.log_monitoring.model.Condition;
import com.log_monitoring.repository.AggSettingRepository;
import com.log_monitoring.repository.ConditionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AggSettingService {

    private final AggSettingRepository aggSettingRepository;
    private final ConditionRepository conditionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 1분간 스케줄러에서 통계 데이터 수집할 설정 저장
    @Transactional
    public Long save(AggSettingSaveRequest request) {
        AggSetting savedAggSetting = aggSettingRepository.save(request.toEntity());
        List<Condition> conditionList =  request.getCondition().stream()
                .map(conditionDto -> conditionDto.toEntity(savedAggSetting)).toList();
        conditionRepository.saveAll(conditionList);
        return savedAggSetting.getId();
    }

    @Transactional
    public void delete(long id) {
        AggSetting aggSetting = aggSettingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AggregationSetting id : " + id + " not found"));
        conditionRepository.deleteAllByAggSettingId(aggSetting.getId());
        aggSettingRepository.delete(aggSetting);
        redisTemplate.delete(generateRedisKey(aggSetting.getTopicName(), aggSetting.getSettingName()));
    }

    public List<AggSettingResponse> findAll() {
        return aggSettingRepository.findAll().stream()
                .map(entity -> {
                    List<ConditionDto> conditionDtoList = conditionRepository.findAllByAggSettingId(entity.getId()).stream()
                            .map(ConditionDto::fromEntity).toList();
                    return AggSettingResponse.fromEntity(entity, conditionDtoList);
                })
                .toList();
    }

    public List<AggSettingResponse> findAllByTopicName(String topicName) {
        return aggSettingRepository.findAllByTopicName(topicName).stream()
                .map(entity -> {
                    List<ConditionDto> conditionDtoList = conditionRepository.findAllByAggSettingId(entity.getId()).stream()
                            .map(ConditionDto::fromEntity).toList();
                    return AggSettingResponse.fromEntity(entity, conditionDtoList);
                })
                .toList();
    }

    public void deleteAllByTopicName(String topicName) {
        List<AggSetting> settings = aggSettingRepository.findAllByTopicName(topicName);
        List<Long> settingIds = settings.stream().map(AggSetting::getId).toList();
        conditionRepository.deleteAllByAggSettingIds(settingIds);
        aggSettingRepository.deleteAllByTopicName(topicName);
        settings.forEach(setting -> redisTemplate.delete(generateRedisKey(topicName, setting.getSettingName())));
    }

    private String generateRedisKey(String topicName, String settingName) {
        return topicName+"_"+settingName;
    }
}

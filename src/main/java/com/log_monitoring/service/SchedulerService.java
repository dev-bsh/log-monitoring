package com.log_monitoring.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log_monitoring.dto.AggSettingResponse;
import com.log_monitoring.dto.LogDataAggSearchRequest;
import com.log_monitoring.dto.LogDataAggSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final AggSettingService aggSettingService;
    private final LogDataService logDataService;
    private final ObjectMapper objectMapper;

    // 1분 단위 실시간 통계 데이터 생성
    public void searchAggregationPerMinute() {
        // DB에 저장된 AggSetting -> AggSettingResponse 형태로 가져오기
        List<AggSettingResponse> realTimeAggSettingList = aggSettingService.findAll();
        // AggSettingResponse -> 토픽 이름별로 매핑
        Map<String, List<LogDataAggSearchRequest.SearchSetting>> aggSettingMap = convertAggSettingToMap(realTimeAggSettingList);
        // 설정별 1분 단위 요청 생성
        List<LogDataAggSearchRequest> aggSearchRequestList = createSearchRequestPerMinute(aggSettingMap);

        // 설정별 1분 단위 조회
        for (LogDataAggSearchRequest searchRequest : aggSearchRequestList) {
            LogDataAggSearchResponse aggSearchResult = logDataService.findAllAggByCondition(searchRequest);
            saveAggSearchResponseInRedis(aggSearchResult); //집계 결과 redis에 저장
            // 웹소켓으로 전송
            messagingTemplate.convertAndSend("/topic/agg/"+aggSearchResult.getTopicName(), aggSearchResult);
        }
    }

    private void saveAggSearchResponseInRedis(LogDataAggSearchResponse aggSearchResult) {
        for (LogDataAggSearchResponse.AggResult aggResult : aggSearchResult.getResult()) {
            // key: topicName_settingName
            String redisKey = generateRedisKey(aggSearchResult.getTopicName(), aggResult.getSettingName());
            redisTemplate.opsForList().rightPush(redisKey, aggResult.getData().get(0));
            // 60개 넘어가면 오래된 값 1개 제거
            if (Objects.requireNonNull(redisTemplate.opsForList().size(redisKey)).intValue() > 60) {
                redisTemplate.opsForList().leftPop(redisKey);
            }
        }
    }

    private Map<String, List<LogDataAggSearchRequest.SearchSetting>> convertAggSettingToMap(List<AggSettingResponse> aggSettingList) {
        Map<String, List<LogDataAggSearchRequest.SearchSetting>> aggSettingMap = new HashMap<>();
        // DB 설정을 토픽별로 구분해서 Map으로 생성
        for (AggSettingResponse realTimeAggSetting : aggSettingList) {
            aggSettingMap.computeIfAbsent(realTimeAggSetting.getTopicName(), k -> new ArrayList<>())
                    .add(LogDataAggSearchRequest.SearchSetting.builder()
                            .settingName(realTimeAggSetting.getSettingName())
                            .conditionList(realTimeAggSetting.getCondition())
                            .build());
        }
        return aggSettingMap;
    }

    private List<LogDataAggSearchRequest> createSearchRequestPerMinute(Map<String, List<LogDataAggSearchRequest.SearchSetting>> aggSettingMap) {
        List<LogDataAggSearchRequest> requestList = new ArrayList<>();
        long now = System.currentTimeMillis();
        long to = now - (now % 60000); // 1분 단위 조회를 위해 초 제거
        for (String topicName : aggSettingMap.keySet()) {
            requestList.add(LogDataAggSearchRequest.builder()
                    .from(to - 60000) // 이전 1분 부터
                    .to(to - 1) // 스케줄 시작시간 - 1ms 까지
                    .topicName(topicName)
                    .searchSettings(aggSettingMap.get(topicName))
                    .build());
        }
        return requestList;
    }

    // redis에 저장된 전체 통계 데이터 조회
    public LogDataAggSearchResponse findAllAggregationInRedisByTopic(String topicName) {
        List<AggSettingResponse> aggSettingList = aggSettingService.findAllByTopicName(topicName);
        List<LogDataAggSearchResponse.AggResult> aggResultList = new ArrayList<>();
        // topic 내의 설정별 데이터 전부 조회
        for (AggSettingResponse aggSetting: aggSettingList) {
            String redisKey = generateRedisKey(topicName, aggSetting.getSettingName());
            List<Object> redisValues = redisTemplate.opsForList().range(redisKey, 0, -1);
            // redis에 저장된 데이터 response로 변환
            List<LogDataAggSearchResponse.AggResult.Data> dataList = objectMapper.convertValue(redisValues, new TypeReference<>() {});
            LogDataAggSearchResponse.AggResult aggResult = LogDataAggSearchResponse.AggResult.builder()
                    .settingName(aggSetting.getSettingName())
                    .data(dataList).build();
            aggResultList.add(aggResult);
        }
        return LogDataAggSearchResponse.builder()
                .topicName(topicName)
                .result(aggResultList)
                .build();
    }

    private String generateRedisKey(String topicName, String settingName) {
        return topicName+"_"+settingName;
    }

}

package com.log_monitoring.controller;

import com.log_monitoring.dto.LogDataAggSearchRequest;
import com.log_monitoring.dto.LogDataAggSearchResponse;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.dto.LogDataSearchResponse;
import com.log_monitoring.service.LogDataService;
import com.log_monitoring.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Log-Data", description = "RawData 및 통계데이터 검색을 위한 컨트롤러")
@RestController
@RequestMapping("/api/log-data")
@RequiredArgsConstructor
public class LogDataController {

    private final LogDataService logDataService;
    private final RedisService redisService;

    // raw data 검색 (시간, 토픽, 조건)
    @Operation(
            summary = "RawData 조회",
            description = "Condition 배열에 들어있는 값 들을 AND 연산, " +
                    "equal: true는 keyword와 완전일치 false는 포함 조건, " +
                    "Condition이 빈 배열인 경우 timestamp와 topicName만으로 조회, " +
                    "조건이 없더라도 Condition: [] 처럼 key, value를 필수로 보내줘야 함"
    )
    @PostMapping
    public ResponseEntity<LogDataSearchResponse> searchLogData(@RequestBody LogDataSearchRequest request) {
        return ResponseEntity.ok(logDataService.findAllByCondition(request));
    }

    @Operation(
            summary = "통계 데이터 조회",
            description = "설정 이름과 조건을 기준으로 시간 단위만큼의 데이터 개수를 집계, " +
                    "return 값 중 settingName이 total_logs는 해당 범위의 전체 로그 개수"
    )
    // 통게 데이터 검색 (시간, 토픽, 조건)
    @PostMapping("/aggregation")
    public ResponseEntity<LogDataAggSearchResponse> searchLogDataAggregation(@RequestBody LogDataAggSearchRequest request) {
        return ResponseEntity.ok(logDataService.findAllAggByCondition(request));
    }

    @Operation(
            summary = "대시보드 통계 데이터 전체 조회",
            description = "특정 토픽이름으로 미리 설정한 대시보드 통계 데이터를 전체 (최대 1시간) 범위로 조회"
    )
    // redis에 저장된 실시간 통계 전체 조회
    @GetMapping("/realtime/{topicName}")
    public ResponseEntity<LogDataAggSearchResponse> getAllRealTimeLogDataAggregations(@PathVariable String topicName) {
        return ResponseEntity.ok(redisService.findAllAggregationInRedisByTopic(topicName));
    }

}

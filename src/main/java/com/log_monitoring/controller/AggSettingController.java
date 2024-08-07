package com.log_monitoring.controller;

import com.log_monitoring.dto.AggSettingResponse;
import com.log_monitoring.dto.AggSettingSaveRequest;
import com.log_monitoring.service.AggSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Aggregation", description = "대시보드에서 확인할 통계데이터의 집계방식 설정 관련 컨트롤러")
@RestController
@RequestMapping("/api/aggregation")
@RequiredArgsConstructor
public class AggSettingController {

    private final AggSettingService aggSettingService;

    @Operation(
            summary = "집계 설정 추가",
            description = "Condition 배열에 들어있는 값 들은 AND 연산, " +
            "equal: true는 keyword와 완전일치, false는 포함 조건"
    )
    @PostMapping
    public ResponseEntity<Long> saveAggregationSetting(@RequestBody AggSettingSaveRequest request) {
        return ResponseEntity.ok(aggSettingService.save(request));
    }

    @Operation(summary = "집계 설정 제거")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAggregationSetting(@PathVariable long id) {
        aggSettingService.delete(id);
        return ResponseEntity.ok("aggregation delete complete id =" + id);
    }

    @Operation(summary = "모든 집계 설정 조회")
    @GetMapping("/listAll")
    public ResponseEntity<List<AggSettingResponse>> getAllAggSettings() {
        return ResponseEntity.ok(aggSettingService.findAll());
    }

    @Operation(summary = "특정 토픽으로 생성된 집계 설정 조회")
    @GetMapping("/list/{topicName}")
    public ResponseEntity<List<AggSettingResponse>> getAllAggSettingsByTopicName(@PathVariable String topicName) {
        return ResponseEntity.ok(aggSettingService.findAllByTopicName(topicName));
    }
}
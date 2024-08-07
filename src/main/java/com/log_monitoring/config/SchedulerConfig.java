package com.log_monitoring.config;

import com.log_monitoring.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final SchedulerService schedulerService;

    @Scheduled(cron = "5 * * * * ?")  // 매 분의 5초마다 실행
    public void schedule() {
        try {
            schedulerService.searchAggregationPerMinute();
            log.info("scheduler is running");
        } catch (Exception e) {
            log.error("[스케줄러 오류] 1분 집계 데이터 생성 오류", e);
        }
    }
}

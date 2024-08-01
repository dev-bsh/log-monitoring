package com.log_monitoring.service;

import com.log_monitoring.model.elasticsearch.LogData;
import com.log_monitoring.repository.elasticsearch.LogDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogDataService {

    private final LogDataRepository logDataRepository;

    public void save(LogData logData) {
        logDataRepository.save(logData);
    }



}

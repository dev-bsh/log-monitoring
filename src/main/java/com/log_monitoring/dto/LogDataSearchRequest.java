package com.log_monitoring.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class LogDataSearchRequest {

    private Long from;
    private Long to;
    private String topicName;

    @Builder
    public LogDataSearchRequest(Long from, Long to, String topicName) {
        this.from = from;
        this.to = to;
        this.topicName = topicName;
    }
}

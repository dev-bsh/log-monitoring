package com.log_monitoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AggSetting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topicName;
    private String fieldName;
    private String keyword;

    @Builder
    public AggSetting(String topicName, String fieldName, String keyword) {
        this.topicName = topicName;
        this.fieldName = fieldName;
        this.keyword = keyword;
    }
}

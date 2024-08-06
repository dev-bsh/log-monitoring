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
    private String settingName;

    @Builder
    public AggSetting(String topicName, String settingName) {
        this.topicName = topicName;
        this.settingName = settingName;
    }
}

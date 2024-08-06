package com.log_monitoring.model;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "conditions")
@NoArgsConstructor
public class Condition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fieldName;
    private String keyword;
    private Boolean equal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agg_setting_id", nullable = false)
    private AggSetting aggSetting;

    @Builder
    public Condition(String fieldName, String keyword, Boolean equal, AggSetting aggSetting) {
        this.fieldName = fieldName;
        this.keyword = keyword;
        this.equal = equal;
        this.aggSetting = aggSetting;
    }
}

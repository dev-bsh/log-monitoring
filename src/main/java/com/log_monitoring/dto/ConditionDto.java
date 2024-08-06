package com.log_monitoring.dto;


import com.log_monitoring.model.AggSetting;
import com.log_monitoring.model.Condition;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ConditionDto {

    private String fieldName;
    private String keyword;
    private Boolean equal;

    @Builder
    public ConditionDto(String fieldName, String keyword, Boolean equal) {
        this.fieldName = fieldName;
        this.keyword = keyword;
        this.equal = equal;
    }

    public static ConditionDto fromEntity(Condition entity) {
        return ConditionDto.builder()
                .fieldName(entity.getFieldName())
                .keyword(entity.getKeyword())
                .equal(entity.getEqual())
                .build();
    }

    public Condition toEntity(AggSetting aggSetting) {
        return Condition.builder()
                .fieldName(fieldName)
                .keyword(keyword)
                .equal(equal)
                .aggSetting(aggSetting)
                .build();
    }

}

package com.log_monitoring.dto;

import com.log_monitoring.model.FieldMetadata;
import com.log_monitoring.model.TopicMetadata;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode
public class FieldDto {

    private String fieldName;
    private String fieldType;

    @Builder
    public FieldDto(String fieldName, String fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public static FieldDto fromEntity(FieldMetadata fieldMetadata) {
        return FieldDto.builder()
                .fieldName(fieldMetadata.getFieldName())
                .fieldType(fieldMetadata.getFieldType())
                .build();
    }

    public FieldMetadata toEntity(TopicMetadata topicMetadata) {
        return FieldMetadata.builder()
                .fieldName(fieldName)
                .fieldType(fieldType)
                .topicMetadata(topicMetadata)
                .build();
    }

}

package com.batch.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDTO {

    private String field1;
    private String field2;
    private String field3;
    private String field4;
}

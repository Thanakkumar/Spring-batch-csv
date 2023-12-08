package com.batch.process.configuration;

import com.batch.process.dto.BatchDTO;
import com.batch.process.entity.BatchEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BatchItemProcessor implements ItemProcessor<BatchDTO, BatchEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchItemProcessor.class);
    AtomicInteger valueOutside
            = new AtomicInteger(0);
    @Override
    public BatchEntity process(final BatchDTO batchDTO) {
        String field1 = batchDTO.getField1();
        String field2 = batchDTO.getField2();
        String field3 = batchDTO.getField3();
        String field4 = batchDTO.getField4();

        BatchEntity transformedEntity = new BatchEntity(String.valueOf(valueOutside.incrementAndGet()),field1, field2, field3, field4);
        LOGGER.info("Converting ( {} ) into ( {} )", batchDTO, transformedEntity);

        return transformedEntity;
    }

}

package com.batch.process.configuration;
import javax.sql.DataSource;

import com.batch.process.dto.BatchDTO;
import com.batch.process.entity.BatchEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;


@Configuration

public class BatchConfiguration {

    @Value("${file.input}")
    private String fileInput;

    @Value("${field.names}")
    private String fieldNames;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfiguration.class);

    @Bean
    public JdbcTransactionManager transactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public FlatFileItemReader reader() {
        String[] fieldName = fieldNames.split(",");
        return new FlatFileItemReaderBuilder<>().name("reader")
                .resource(new ClassPathResource(fileInput))
                .delimited()
                .names(new String[] { fieldName[0], fieldName[1], fieldName[2],fieldName[3] })
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(BatchDTO.class);
                }})
                .build();
    }

    @Bean
    public BatchItemProcessor processor() {
        LOGGER.info("inside processor");
        return new BatchItemProcessor();
    }


    @Bean
    public JdbcBatchItemWriter writer(DataSource dataSource) {
        String[] fieldName = fieldNames.split(",");
        LOGGER.info("inside writer");
        return new JdbcBatchItemWriterBuilder<BatchEntity>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO BATCH VALUES (:id, :"+fieldName[0]+", :"+fieldName[1]+", :"+fieldName[2]+", :"+fieldName[3]+");")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    //@Profile("nosql")
    public MongoItemWriter<BatchEntity> mongoWriter() {
        MongoItemWriter<BatchEntity> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("Batch");
        return writer;
    }

    @Bean
   // @ConditionalOnProperty(name="run")
    public Job job(JobRepository jobRepository, DataSourceTransactionManager transactionManager) {
        LOGGER.info("inside job");
        return new JobBuilder("job", jobRepository)
                .start(createStep(jobRepository, transactionManager,transactionManager.getDataSource()))
                .build();
    }

    @Bean
    //@Profile("nosql")
    public Job jobNoSql(JobRepository jobRepository, DataSourceTransactionManager transactionManager) {
        LOGGER.info("inside jobNoSql");
        return new JobBuilder("jobNoSql", jobRepository)
                .start(createStepMongo(jobRepository, transactionManager,transactionManager.getDataSource()))
                .build();
    }

    @Bean
    public Step createStep(JobRepository jobRepository, DataSourceTransactionManager transactionManager, DataSource dataSource) {
        LOGGER.info("inside createStep");
        return new StepBuilder("createStep", jobRepository)
                .allowStartIfComplete(true)
                .<BatchDTO, BatchEntity> chunk(1, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer(dataSource))
                .build();
    }

    @Bean
    //@Profile("nosql")
    public Step createStepMongo(JobRepository jobRepository, DataSourceTransactionManager transactionManager, DataSource dataSource) {
        LOGGER.info("inside createStep mongo");
        return new StepBuilder("createStepMongo", jobRepository)
                .allowStartIfComplete(true)
                .<BatchDTO, BatchEntity> chunk(1, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(mongoWriter())
                .build();
    }
}

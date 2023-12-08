package com.batch.process;

import com.batch.process.dto.BatchDTO;
import com.batch.process.entity.BatchEntity;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BatchController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired(required = false)
    @Qualifier("jobNoSql")
    private Job jobNoSql;

    @Autowired(required = false)
    @Qualifier("job")
    private Job jobSql;

    @Autowired(required = false)
    private JobLauncher jobLauncher;


    @GetMapping("/batch")
    public ResponseEntity<List<BatchEntity>> getAllBatchRecords() {
        String query = "SELECT * FROM BATCH";
       return new ResponseEntity<>(jdbcTemplate.query(query, (rs, row) -> new BatchEntity(rs.getString(1), rs.getString(2), rs.getString(3),rs.getString(4), rs.getString(5))), HttpStatus.OK);
    }
    @GetMapping("/nosql/batch")
    public ResponseEntity<List<BatchEntity>> getAllMongoBatchRecords() {
        List<BatchEntity> records = mongoTemplate.findAll(BatchEntity.class,"Batch");
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    @GetMapping("/batch/{id}")
    public ResponseEntity<BatchEntity> getAllBatchRecords(@PathVariable String id) {
        String query = "SELECT * FROM BATCH where id = "+ id;
        List<BatchEntity> response = jdbcTemplate.query(query, (rs, row) -> new BatchEntity(rs.getString(1), rs.getString(2), rs.getString(3),rs.getString(4), rs.getString(5)));
        if(response == null || CollectionUtils.isEmpty(response)){
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        return new ResponseEntity<>(response.get(0), HttpStatus.OK);
    }

    @GetMapping("/nosql/batch/{id}")
    public ResponseEntity<BatchEntity> getAllBatchRecordsMongo(@PathVariable String id) {
        BatchEntity response = mongoTemplate.findById(id,BatchEntity.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/run/nosql")
    public String runBatchNoSql() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobExecution execution1 = jobLauncher.run(jobNoSql, new JobParameters());
        System.out.println("Exit Status Nosql: " + execution1.getStatus());
        return "NoSql Job hit successfully";
    }

    @GetMapping("/run/sql")
    public String runBatchSql() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobExecution execution2 = jobLauncher.run(jobSql, new JobParameters());
        System.out.println("Exit Status Sql : " + execution2.getStatus());
       return "Sql Job hit successfully";
    }
}

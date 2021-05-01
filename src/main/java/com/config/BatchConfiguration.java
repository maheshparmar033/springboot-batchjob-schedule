package com.config;

import com.listener.JobCompletionListener;
import com.step.Employee;
import com.step.EmployeeProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Value("classpath:/input/InputData.csv")
    private Resource inputResource;

    @Autowired
    DataSource dataSource;

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(15);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(30);
        return taskExecutor;
    }

    @Bean
    public Job processJob1() {
        return jobBuilderFactory.get("processJob1")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(orderStep())
                .end()
                .build();
    }

    @Bean
    public Job processJob2() {
        return jobBuilderFactory.get("processJob2")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(orderStep())
                .end()
                .build();
    }

    @Bean
    public Step orderStep() {
        return stepBuilderFactory
                .get("step")
                .<Employee, Employee>chunk(5)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

    @Bean
    public ItemProcessor<Employee, Employee> processor() {
        return new EmployeeProcessor();
    }

    @Bean
    public FlatFileItemReader<Employee> reader() {
        FlatFileItemReader<Employee> itemReader = new FlatFileItemReader<Employee>();
        itemReader.setLineMapper(lineMapper());
        itemReader.setLinesToSkip(1);
        itemReader.setResource(inputResource);
        return itemReader;
    }

    @Bean
    public LineMapper<Employee> lineMapper() {
        DefaultLineMapper<Employee> lineMapper = new DefaultLineMapper<Employee>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(new String[] { "id", "firstName", "lastName" });
        lineTokenizer.setIncludedFields(new int[] { 0, 1, 2 });
        BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<Employee>();
        fieldSetMapper.setTargetType(Employee.class);
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    public JdbcBatchItemWriter<Employee> writer() {
        JdbcBatchItemWriter<Employee> itemWriter = new JdbcBatchItemWriter<Employee>();
        itemWriter.setDataSource(dataSource);
        itemWriter.setSql("INSERT INTO EMPLOYEE (ID, FIRSTNAME, LASTNAME) VALUES (:id, :firstName, :lastName)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Employee>());
        return itemWriter;
    }

}

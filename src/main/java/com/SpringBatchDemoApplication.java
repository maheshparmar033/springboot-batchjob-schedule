package com;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class SpringBatchDemoApplication {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job processJob1;

	@Autowired
	private Job processJob2;

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchDemoApplication.class, args);
	}

	@Scheduled(cron = "0 */1 * * * ?")
	public void performJob1() throws Exception {
		JobParameters params = new JobParametersBuilder().addString("JobID1", String.valueOf(System.currentTimeMillis()))
														 .toJobParameters();
		jobLauncher.run(processJob1, params);
	}

	@Scheduled(cron = "0 */1 * * * ?")
	public void performJob2() throws Exception {
		JobParameters params = new JobParametersBuilder().addString("JobID2", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(processJob2, params);
	}

}

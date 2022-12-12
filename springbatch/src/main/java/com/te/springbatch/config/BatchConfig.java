package com.te.springbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.te.springbatch.entity.Customer;
import com.te.springbatch.repository.CustomerRepo;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobBuilderFactory builderFactory;

	@Autowired
	private StepBuilderFactory factory;

	@Autowired
	private CustomerRepo repo;

	@Bean
	public FlatFileItemReader<Customer> reader() {
		FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource("src/main/resources/Customer.csv"));
		itemReader.setName("CSV_READER");
		itemReader.setLinesToSkip(1);
		itemReader.setLineMapper(lineMapper());
		return itemReader;
	}

	private LineMapper<Customer> lineMapper() {
		DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("name", "gender", "email", "mobileNumber");
		BeanWrapperFieldSetMapper<Customer> mapper = new BeanWrapperFieldSetMapper<>();
		mapper.setTargetType(Customer.class);
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(mapper);
		return lineMapper;
	}

	@Bean
	public CustomerProcessor processor() {
		return new CustomerProcessor();
	}

	@Bean
	public RepositoryItemWriter<Customer> writer() {
		RepositoryItemWriter<Customer> writter = new RepositoryItemWriter<>();
		writter.setRepository(repo);
		writter.setMethodName("save");
		return writter;
	}

	@Bean
	public org.springframework.batch.core.Step step1() {
		return factory.get("csv-step").<Customer, Customer>chunk(10).reader(reader()).processor(processor())
				.writer(writer()).taskExecutor(executor()).build();
	}

	@Bean
	public Job runJob() {
		return builderFactory.get("importCustomersInformation").flow(step1()).end().build();
	}
	
	@Bean
	public TaskExecutor executor() {
		SimpleAsyncTaskExecutor executor=new SimpleAsyncTaskExecutor();
		executor.setConcurrencyLimit(10);
		return executor;
	}
}

package demo.configuration;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import demo.mapper.PersonRowMapper;
import demo.model.Person;
import demo.processor.PersonItemProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	@Autowired
	@Qualifier("ds1")
	private DataSource ds1;
	
	@Autowired
	@Qualifier("ds2")
	private DataSource ds2;

	@Autowired
	private JobBuilderFactory jobs;
	
	@Autowired
	private StepBuilderFactory steps;

	@Bean(name="ds1")
	@Primary
	@ConfigurationProperties(prefix="spring.ds1")
	public DataSource primaryDS() {
	    return DataSourceBuilder.create().build();
	}

	@Bean(name = "ds2")
	@ConfigurationProperties(prefix="spring.ds2")
	public DataSource secondaryDS() {
	    return DataSourceBuilder.create().build();
	}
	
	
	@Bean
	BatchConfigurer configurer(DataSource dataSource){
	  return new DefaultBatchConfigurer(dataSource);
	}
	
	
	@Bean
	public Job importUserJob() {
		return jobs.get("databaseToDatabaseJob")
				.incrementer(new RunIdIncrementer())
				.start(step1())
				.build();
	}

	@Bean
	public Step step1() {
		return steps.get("databaseToDatabaseStep")
				.<Person, Person> chunk(2)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
	
	
	

	
	@Bean
	public ItemStreamReader<Person> reader() {
		JdbcCursorItemReader<Person> reader = new JdbcCursorItemReader<Person>();
		reader.setDataSource(ds1);
		reader.setSql("SELECT first_name, last_name FROM person");
		reader.setRowMapper(new PersonRowMapper());
		return reader;
	}

	@Bean
	public ItemProcessor<Person, Person> processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public ItemWriter<Person> writer() {
		JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		writer.setDataSource(ds2);
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
		return writer;
	}
	
}

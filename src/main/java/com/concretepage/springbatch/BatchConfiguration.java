package com.concretepage.springbatch;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Bean
    public ItemReader<Student> reader() {
        FlatFileItemReader<Student> reader = new FlatFileItemReader<Student>();
        reader.setResource(new ClassPathResource("student-data.csv"));
        reader.setLineMapper(new DefaultLineMapper<Student>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] {"stdId", "subMarkOne", "subMarkTwo" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Student>() {{
                setTargetType(Student.class);
            }});
        }});
        return reader;
    }
   

    @Bean
    public ItemWriter<Marksheet> writer(DataSource dataSource) {
        JdbcBatchItemWriter<Marksheet> writer = new JdbcBatchItemWriter<Marksheet>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Marksheet>());
        writer.setSql("INSERT INTO marksheet (stdId,totalSubMark) VALUES (:stdId,:totalSubMark)");
        writer.setDataSource(dataSource);
        return writer;
    }


    @Bean
    public ItemWriter<Marksheet> writer1() {
        FlatFileItemWriter<Marksheet> writer = new FlatFileItemWriter<Marksheet>();
       // writer.setResource(new ClassPathResource("student-marksheet.csv"));
        writer.setResource(new FileSystemResource("out/production/resources/student-marksheet.csv"));
        DelimitedLineAggregator<Marksheet> delLineAgg = new DelimitedLineAggregator<Marksheet>();
        delLineAgg.setDelimiter(",");
        BeanWrapperFieldExtractor<Marksheet> fieldExtractor = new BeanWrapperFieldExtractor<Marksheet>();
        fieldExtractor.setNames(new String[] {"stdId", "totalSubMark"});
        delLineAgg.setFieldExtractor(fieldExtractor);
        writer.setLineAggregator(delLineAgg);
        return writer;
    }


    //xml file

    @Bean
    public ItemWriter<Marksheet> xmlWriter() {
        StaxEventItemWriter<Marksheet> xmlFileWriter = new StaxEventItemWriter<>();


        xmlFileWriter.setResource(new FileSystemResource("out/production/resources/students.xml"));

        xmlFileWriter.setRootTagName("marksheet");

        Jaxb2Marshaller markSheetMarshaller = new Jaxb2Marshaller();
        markSheetMarshaller.setClassesToBeBound(Marksheet.class);
        xmlFileWriter.setMarshaller(markSheetMarshaller);

        return xmlFileWriter;
    }



    @Bean
    public ItemProcessor<Student, Marksheet> processor() {
        return new StudentItemProcessor();
    }

    @Bean
    public Job createMarkSheet(JobBuilderFactory jobs, Step step,Step step1) {
        return jobs.get("createMarkSheet")
                .flow(step)
               .next(step1)
                .end()
                .build();
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<Student> reader,
            ItemWriter<Marksheet> writer, ItemProcessor<Student, Marksheet> processor,ItemWriter<Marksheet> writer1) {
        return stepBuilderFactory.get("step")
                .<Student, Marksheet> chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .writer(writer1)
                .build();
    }


//    @Bean
//    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<Student> reader,
//                     ItemWriter<Marksheet> writer, ItemProcessor<Student, Marksheet> processor) {
//        return stepBuilderFactory.get("step")
//                .<Student, Marksheet> chunk(5)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .build();
//    }
//
//    @Bean
//    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//
//    @Bean
//	public DataSource getDataSource() {
//	    BasicDataSource dataSource = new BasicDataSource();
//	    dataSource.setDriverClassName("org.h2.Driver");
//	    dataSource.setUrl("jdbc:h2:file:~/test");
//	    dataSource.setUsername("sa");
//	    dataSource.setPassword("");
//	    return dataSource;
//	}

}

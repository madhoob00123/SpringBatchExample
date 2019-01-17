package com.concretepage.springbatch;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
@ComponentScan
@EnableAutoConfiguration
public class Main {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Main.class, args);
        List<Marksheet> result = ctx.getBean(JdbcTemplate.class).query("select stdId,totalSubMark FROM marksheet",
        		new RowMapper<Marksheet>() {
            @Override
            public Marksheet mapRow(ResultSet rs, int row) throws SQLException {
                return new Marksheet(rs.getString(1), Integer.parseInt(rs.getString(2)));
            }
        });

        System.out.println("Number of Record:"+result.size());
        result.forEach(System.out::println);

    }
}

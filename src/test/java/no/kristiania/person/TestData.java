package no.kristiania.person;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class TestData {

    public static DataSource TestDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:persondb");
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}

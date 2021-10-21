package no.kristiania.person;

import javax.sql.DataSource;

public class PersonDao {
    private final DataSource dataSource;

    public PersonDao(DataSource testDataSource) {
        this.dataSource = testDataSource;
    }

    public void save(Person person) {

    }

    public Person retrieve(long id) {
        return null;
    }
}
